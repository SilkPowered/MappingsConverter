package cx.rain.mc.mappingsConverter;

import net.fabricmc.lorenztiny.TinyMappingFormat;
import net.fabricmc.lorenztiny.TinyMappingsLegacyWriter;
import net.fabricmc.lorenztiny.TinyMappingsReader;
import net.fabricmc.lorenztiny.TinyMappingsWriter;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.proguard.ProGuardReader;
import org.cadixdev.lorenz.io.srg.csrg.CSrgReader;

import java.io.*;
import java.nio.file.Path;

public class Main {
    private static final String VERSION = "1.18.1";
    public static void main(String[] args) throws IOException {
        var tinyO2IFile = new File(VERSION + ".yarn.tiny");
        var o2ITiny = TinyMappingFormat.TINY.read(tinyO2IFile.toPath(), "official", "intermediary");

        var o2BClassReader = new BufferedReader(new FileReader("bukkit-" + VERSION + "-cl.csrg"));
        var o2BClass = new CSrgReader(o2BClassReader).read();

        var no2OClientReader = new BufferedReader(new FileReader("client.txt"));
        var no2OClient = new ProGuardReader(no2OClientReader).read();

        var no2OServerReader = new BufferedReader(new FileReader("client.txt"));
        var no2OServer = new ProGuardReader(no2OServerReader).read();

        var no2OMerged = MappingSet.create().merge(no2OClient).merge(no2OServer);

//        o2ITiny = o2ITiny.merge(o2BClass.reverse());
        o2BClass = o2BClass.reverse().merge(o2ITiny).reverse();

        var bufferedWriterTiny = new BufferedWriter(new FileWriter("silk-" + VERSION + ".tiny"), 1024 * 1024 * 10);
        var writerTiny = new TinyMappingsLegacyWriter(bufferedWriterTiny, "intermediary", "bukkit");
        writerTiny.write(o2BClass);
        bufferedWriterTiny.flush();
        bufferedWriterTiny.close();
    }
}
