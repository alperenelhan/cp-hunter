package org.elhan.cphunter;

import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.json.JSONException;

/**
 * Hello world!
 *
 */
public class App {

    /**
     * @param args
     */
    public static void main(String[] args) throws JSONException, IOException, ParseException {
        Options options = new Options();
        Parser parser = new GnuParser();
        Option classes = OptionBuilder.withLongOpt("class").hasArgs().withArgName("CLASSNAME").isRequired(true).withDescription("Name of the class").create("c");
        Option directories = OptionBuilder.withLongOpt("dir").hasArgs().withArgName("DIR").isRequired(false).withDescription("Where to search for classes").create("d");
        Option help = OptionBuilder.withLongOpt("help").hasArg(false).withDescription("Shows this menu").create("h");
        Option withRunning = OptionBuilder.withLongOpt("withRunning").hasArg(false).withDescription("Search in running processes").create("r");
        options.addOption(classes);
        options.addOption(directories);
        options.addOption(withRunning);
        options.addOption(help);
        String cmdname = "cp-hunt";
        String header = "Available options are:";
        String footer = "Please report issues at https://github.com/alperenelhan/cp-hunter";
        try {
            CommandLine opts = parser.parse(options, args);
            if (opts.hasOption("h")) {
                HelpFormatter helper = new HelpFormatter();
                helper.printHelp(cmdname, header, options, footer, true);
                return;
            }
            String [] classValues = opts.getOptionValues("c");
            String [] dirValues = null;
            if (opts.hasOption("d")) {
                dirValues = opts.getOptionValues("d");
            }
            
            
            CPHunter cp = new CPHunter(classValues, dirValues, opts.hasOption("r"));
            cp.hunt();
            
            
        } catch (Exception e) {
            HelpFormatter helper = new HelpFormatter();
            helper.printHelp(cmdname, header, options, footer, true);
        }

    }
}
