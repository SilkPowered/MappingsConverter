package cx.rain.mc.mappingsConverter;

import net.fabricmc.lorenztiny.TinyMappingFormat;
import net.fabricmc.lorenztiny.TinyMappingsLegacyWriter;
import net.fabricmc.lorenztiny.TinyMappingsReader;
import net.fabricmc.lorenztiny.TinyMappingsWriter;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.proguard.ProGuardReader;
import org.cadixdev.lorenz.io.srg.SrgWriter;
import org.cadixdev.lorenz.io.srg.csrg.CSrgReader;

import java.io.*;
import java.nio.file.Path;

public class Main {
    private static final String VERSION = "1.18.1";
    public static void main(String[] args) throws IOException {
//        var tinyO2IFile = new File(VERSION + ".tiny");
//        var o2ITiny = TinyMappingFormat.TINY.read(tinyO2IFile.toPath(), "official", "intermediary");
//        var o2NTiny = TinyMappingFormat.TINY.read(tinyO2IFile.toPath(), "official", "named");

//        var tinyO2IFileV2 = new File(VERSION + ".yarn.v2.tiny");
//        var o2ITiny = TinyMappingFormat.TINY_2.read(tinyO2IFileV2.toPath(), "official", "intermediary");

        var tinyO2IFileV2 = new File(VERSION + ".workspace.v2.tiny");
        var o2ITiny = TinyMappingFormat.TINY_2.read(tinyO2IFileV2.toPath(), "official", "intermediary");


        var o2BClassReader = new BufferedReader(new FileReader("bukkit-" + VERSION + "-cl.csrg"));
        var o2BClass = new CSrgReader(o2BClassReader).read();

        var no2OClientReader = new BufferedReader(new FileReader("client.txt"));
        var no2OClient = new ProGuardReader(no2OClientReader).read();

        var no2OServerReader = new BufferedReader(new FileReader("server.txt"));
        var no2OServer = new ProGuardReader(no2OServerReader).read();

        var no2OMerged = MappingSet.create().merge(no2OClient).merge(no2OServer);

        var merged = MappingSet.create().merge(no2OMerged).reverse().merge(o2ITiny).merge(o2BClass);
//        var merged = o2BClass.reverse().merge(o2ITiny).reverse();

        var bufferedWriterTiny = new BufferedWriter(new FileWriter("silk-" + VERSION + ".tiny"), 1024 * 1024 * 10);
        var writerTiny = new TinyMappingsLegacyWriter(bufferedWriterTiny, "intermediary", "bukkit");
        writerTiny.write(merged);
//        var writerTiny2 = new TinyMappingsLegacyWriter(bufferedWriterTiny, "official", "bukkit");
//        writerTiny2.write(merged);
        bufferedWriterTiny.flush();
        bufferedWriterTiny.close();


//        o2ITiny = o2ITiny.merge(o2BClass.reverse());
//        var o2BClass2 = o2BClass.reverse().merge(o2ITiny).reverse();
//        var o2NClass = o2BClass.reverse().merge(o2NTiny).reverse();
//
//        var bufferedWriterTiny = new BufferedWriter(new FileWriter("silk-" + VERSION + ".tiny"), 1024 * 1024 * 10);
//        var writerTiny = new TinyMappingsLegacyWriter(bufferedWriterTiny, "intermediary", "bukkit");
//        writerTiny.write(o2BClass2);
//        bufferedWriterTiny.flush();
//        bufferedWriterTiny.close();
//
//        var bufferedWriterTiny2 = new BufferedWriter(new FileWriter("silk-" + VERSION + ".n2b.tiny"), 1024 * 1024 * 10);
//        var writerTiny2 = new TinyMappingsLegacyWriter(bufferedWriterTiny2, "named", "bukkit");
//        writerTiny2.write(o2NClass);
//        bufferedWriterTiny2.flush();
//        bufferedWriterTiny2.close();
//
//        var bufferedWriterSrg = new BufferedWriter(new FileWriter("silk-" + VERSION + "-rev.srg"), 1024 * 1024 * 10);
//        var writerSrg = new SrgWriter(bufferedWriterSrg);
//        writerSrg.write(o2BClass2.reverse());
//        bufferedWriterSrg.flush();
//        bufferedWriterSrg.close();


    }
}
