import cipher.Shamir;
import cipher.ShamirPoint;
import common.MathUtils;
import common.Mode;
import exceptions.InvalidParameterException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.ArrayList;
import java.util.List;


public class Main {

    private static Mode mode;
    private static int minimumPoints;
    private static int totalPoints;
    private static int secret;
    private static int primaryNumber;
    private static List<ShamirPoint> points;

    public static void main(String[] args) {

        try {
            extractCommandLineArguments(args);
            Shamir shamir = new Shamir();

            switch (mode) {
                case Split:
                    List<ShamirPoint> generatedPoints = shamir.splitSecret(secret, minimumPoints, totalPoints, primaryNumber);
                    System.out.println("GENERATED POINTS: ");
                    for (ShamirPoint generatedPoint : generatedPoints) {
                        System.out.println(generatedPoint.toString());
                    }
                    break;
                case Join:
                    if (points == null || points.isEmpty()) //pré-conditions sur les points
                        throw  new InvalidParameterException("No point were specified to find secret");
                    int foundSecret = shamir.joinSecret(points, primaryNumber);
                    System.out.printf("Secret is : %d", foundSecret);
                    break;
            }

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Extraction des arguments avec apache.common.cli
     * Ce code est assez explicite sur ce qu'il fait: on ajoute les arguments possibles comme 'Option',
     * puis on extrait les arguments réels en sa basant sur ces options.
     * */
    private static void extractCommandLineArguments(String[] args) throws ParseException {
        Options options = new Options();

        Option encryptArg = new Option("e", false, "separate secret mode");
        encryptArg.setRequired(false);
        options.addOption(encryptArg);

        Option decryptArg = new Option("d", false, "find secret mode");
        decryptArg.setRequired(false);
        options.addOption(decryptArg);

        Option kArg = new Option("k", true, "min number of points to solve secret");
        kArg.setRequired(false);
        options.addOption(kArg);

        Option nArg = new Option("n", true, "total number of points");
        nArg.setRequired(false);
        options.addOption(nArg);

        Option secretArg = new Option("s", true, "THE SECRET NUMBER");
        secretArg.setRequired(false);
        options.addOption(secretArg);

        Option qArg = new Option("q", true, "primary number for Shamir algorithm");
        qArg.setRequired(true);
        options.addOption(qArg);

        Option pArg = new Option("p", true, "points for secret resolution");
        pArg.setArgs(2);
        pArg.setRequired(false);
        options.addOption(pArg);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("utility-name", options);
            throw e;
        }

        extractModeArgument(cmd);
        extractSimpleArguments(cmd);
        extractPointsArg(cmd);
    }

    private static void extractPointsArg(CommandLine cmd) {
        String[] points = cmd.getOptionValues("p");
        if (points != null) {
            Main.points = new ArrayList<>();
            for (int i = 0; i < points.length; i += 2) {

                int x = Integer.parseInt(points[i]);
                int y = Integer.parseInt(points[i+1]);
                Main.points.add(new ShamirPoint(x, y)); //On construit l'objet ShamirPoint à partir des x et y passés en paramètre.
            }
        }
    }

    private static void extractSimpleArguments(CommandLine cmd) {
        String kValue = cmd.getOptionValue("k");
        String nValue = cmd.getOptionValue("n");
        String secretValue = cmd.getOptionValue("s");
        String qValue = cmd.getOptionValue("q");
        if (kValue != null && !kValue.isEmpty()) {
            Main.minimumPoints = Integer.parseInt(kValue);
        }
        if (nValue != null && !nValue.isEmpty()) {
            Main.totalPoints = Integer.parseInt(nValue);
        }
        if (secretValue != null && !secretValue.isEmpty()) {
            Main.secret = Integer.parseInt(secretValue);
        }
        if (qValue != null && !qValue.isEmpty()) {
            Main.primaryNumber = Integer.parseInt(qValue);
            if (!MathUtils.isPrime(Main.primaryNumber)) //q doit absoluement être un nombre premier
                throw new InvalidParameterException("Parameter -q needs to be a prime number");
        }
    }

    /**
     * Le mode d'opération (e ou d) est convertit en enum constant pour faciliter la lisibilité du code et la compréhension.
     * */
    private static void extractModeArgument(CommandLine cmd) {
        boolean doSplitSecret = cmd.hasOption("e");
        boolean doJoinSecret = cmd.hasOption("d");

        if (doSplitSecret && !doJoinSecret)
            Main.mode = Mode.Split;
        else if (!doSplitSecret && doJoinSecret)
            Main.mode = Mode.Join;
        else
            throw  new InvalidParameterException("No valid mode was provided as argument. valid mode are: -e, -d");
    }
}
