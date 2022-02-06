package cx.rain.mc.mappingsConverter;

import net.fabricmc.lorenztiny.TinyMappingFormat;
import net.fabricmc.lorenztiny.TinyMappingsWriter;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.proguard.ProGuardReader;
import org.cadixdev.lorenz.io.srg.csrg.CSrgReader;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;

import java.io.*;

public class Main {
    private static final String VERSION = "1.18.1";

    private static final MappingSet MAPPING_SET = MappingSet.create();

    public static void main(String[] args) throws IOException {
        var intermediaryV2File = new File(VERSION + ".workspace.v2.tiny");
        var intermediary = TinyMappingFormat.TINY_2.read(intermediaryV2File.toPath(), "official", "intermediary");

        var bukkitClassReader = new BufferedReader(new FileReader("bukkit-" + VERSION + "-cl.csrg"));
        var bukkitClass = new CSrgReader(bukkitClassReader).read();

        var officialClientReader = new BufferedReader(new FileReader("client.txt"));
        var officialClient = new ProGuardReader(officialClientReader).read();
        var officialServerReader = new BufferedReader(new FileReader("server.txt"));
        var officialServer = new ProGuardReader(officialServerReader).read();

        var officialMerged = MappingSet.create().merge(officialClient).merge(officialServer).reverse();

//        for (ClassMapping<?, ?> classMapping : officialMerged.getTopLevelClassMappings()) {
//            process(classMapping, intermediary, bukkitClass);
//        }

        var bufferedWriterTiny = new BufferedWriter(new FileWriter("silk-" + VERSION + ".tiny"), 1024 * 1024 * 10);
        var writerTiny = new TinyMappingsWriter(bufferedWriterTiny, "official", "bukkit");
//        writerTiny.write(MAPPING_SET);
        writerTiny.write(bukkitClass);
        bufferedWriterTiny.flush();
        bufferedWriterTiny.close();
    }

//    private static void process(ClassMapping<?, ?> classMapping, MappingSet intermediaryMapping, MappingSet officialMapping) {
//        var officialClass = classMapping.getFullObfuscatedName();
//        var bukkitClass = classMapping.getFullDeobfuscatedName();
//        var intermediaryClass = intermediaryMapping.getClassMapping(officialClass).get();
//        MAPPING_SET.createTopLevelClassMapping(bukkitClass,
//                intermediaryClass.getFullDeobfuscatedName());
//
//        var clazz = MAPPING_SET.getTopLevelClassMapping(bukkitClass).get();
//        for (FieldMapping officialField : officialMapping.getClassMapping(officialClass).get().getFieldMappings()) {
//            var field = officialField.getFullObfuscatedName();
//            var optionalField = intermediaryClass.getFieldMapping(field);
//            if (optionalField.isPresent()) {
//                clazz.createFieldMapping(optionalField.get().getFullDeobfuscatedName(), field);
//            } else {
//                System.out.println("Field mapping " + field + " is missing, please check your mapping.");
//            }
//        }
//
////        for (FieldMapping fieldMapping : officialMapping.getClassMapping(officialClass).get().getFieldMappings()) {
////            var field = fieldMapping.getFullObfuscatedName();
////            var intermediaryField = intermediaryClass.getFieldMapping(field).get();
////            clazz.createFieldMapping(intermediaryField.getFullDeobfuscatedName(), field);
////        }
//
//        for (ClassMapping<?, ?> innerClass : classMapping.getInnerClassMappings()) {
//            process(innerClass, intermediaryMapping, officialMapping);
//        }
//    }

    private static void process(ClassMapping<?, ?> classMapping,
                                MappingSet intermediaryMapping, MappingSet bukkitMapping) {
        checkAndWriteClass(classMapping, intermediaryMapping, bukkitMapping);

        for (ClassMapping<?, ?> innerClass : classMapping.getInnerClassMappings()) {
            process(innerClass, intermediaryMapping, bukkitMapping);
        }
    }

    private static void checkAndWriteClass(ClassMapping<?, ?> officialClazz, MappingSet intermediary, MappingSet bukkit) {
        var name = officialClazz.getFullObfuscatedName();
        var optionalIntermediaryClass =
                intermediary.getClassMapping(name);
        var optionalBukkitClass = bukkit.getClassMapping(name);

        ClassMapping<?, ?> clazz;
        if (optionalIntermediaryClass.isPresent()) {
            if (optionalBukkitClass.isPresent()) {
                clazz = MAPPING_SET.createTopLevelClassMapping(
                        optionalIntermediaryClass.get().getFullDeobfuscatedName(),
                        optionalBukkitClass.get().getFullDeobfuscatedName());
            } else {
                System.out.println("Top-level class mapping " + name + " has no Bukkit map.");
                clazz = MAPPING_SET.createTopLevelClassMapping(
                        optionalIntermediaryClass.get().getFullDeobfuscatedName(), name);
            }

            // Fixme
//            for (FieldMapping field : officialClazz.getFieldMappings()) {
//                checkAndWriteField(field, clazz, optionalIntermediaryClass.get());
//            }

        } else {
            if (optionalBukkitClass.isPresent()) {
                System.out.println("Top-level class mapping " + name + " has no Intermediary map.");
                clazz = MAPPING_SET.createTopLevelClassMapping(name,
                        optionalBukkitClass.get().getFullDeobfuscatedName());
                // Fixme
//                for (FieldMapping field : officialClazz.getFieldMappings()) {
//                    checkAndWriteField(field, clazz, clazz);
//                }
            } else {
                System.out.println("Top-level class mapping " + name + " has no Bukkit neither Intermediary map.");
//                clazz = MAPPING_SET.createTopLevelClassMapping(name, name);
            }
        }
    }

    private static void checkAndWriteField(FieldMapping field, ClassMapping<?, ?> clazz,
                                           ClassMapping<?, ?> intermediaryClass) {
        var name = field.getFullObfuscatedName();

        // qyl: There must be a correct class mapping for field mapping.
        var optionalIntermediaryField = intermediaryClass.getFieldMapping(name);

        for (FieldMapping f : intermediaryClass.getFieldMappings()) {
            if (f.getFullObfuscatedName().equals(name)) {
                clazz.createFieldMapping(f.getFullDeobfuscatedName(), name);
            }
        }

//        // qyl: There is no bukkit map for fields.
//        if (optionalIntermediaryField.isPresent()) {
//            clazz.createFieldMapping(optionalIntermediaryField.get().getFullDeobfuscatedName(), name);
//        } else {
//            System.out.println("Field mapping " + name + " has no Intermediary map.");
////            clazz.createFieldMapping(name, name);
//        }
    }
}
