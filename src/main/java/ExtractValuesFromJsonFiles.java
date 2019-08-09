import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *The function gets a path to a root folder and a key for which we want to extract
 *  all the values contained in json files located in the root and subfolders of the
 *  root directory and prints the amount of the different values and the values
 *  themselves as a list.
 */
public class ExtractValuesFromJsonFiles {
    public static void main(String args[]){

        // ===============================================================================
        //
        //  TODO: Enter arguments for the key for which you
        //        want to extract all the values and path to the
        //        root folder where all the json files are stored.
        //        for example:
        //        -requireKey "Last name" -rootDirPath C:/Users/xxx/Desktop/rootDirectory
        //
        // ===============================================================================


        // Command line parser
        Options options    = new Options();
        Option reqeKey     = new Option("requireKey"  ,true,"The key for which the information will be extracted");
        Option rootDirPath = new Option("rootDirPath", true,"Path to the root directory");

        // Some configuration
        reqeKey.setRequired(true);
        reqeKey.setType(String.class);
        options.addOption(reqeKey);

        rootDirPath.setRequired(true);
        rootDirPath.setType(String.class);
        options.addOption(rootDirPath);

        CommandLineParser parser    = new DefaultParser();
        HelpFormatter     formatter = new HelpFormatter();
        try{
            CommandLine cmd;
            cmd = parser.parse(options, args);

            // Parsing the commad line arguments
            final String requireKey = cmd.getOptionValue("requireKey");
            final Path   dirName    = Paths.get(cmd.getOptionValue("rootDirPath"));

            LinkedList answer = new LinkedList();

            // Recursive walk from root folder through all sub folders and return the path to akk json file.
            Stream<Path> myStream = Files.walk(dirName, Integer.MAX_VALUE);
            List<Path> allJsonsFilePaths = myStream.filter(path -> path.getFileName().toString().endsWith(".json")).collect(Collectors.toList());

            // For any json file we make recursive search after the values corresponding to requireKey
            for (Path p: allJsonsFilePaths) {
                JSONParser jsonParser = new JSONParser();
                JSONObject currObject = (JSONObject) jsonParser.parse(new FileReader(p.toString()));
                findKeyRecursive(currObject, requireKey, new LinkedList<>()).
                        forEach(value ->{
                            // We add the value to the answer list if it's not already in it
                            if(!answer.contains(value)) answer.add(value);
                        });
            }
            if(!answer.isEmpty()) {
                System.out.println("There are a total of " + answer.size() + " different values under the key: \""+requireKey + "\"\n");
                System.out.println("Values :" + answer);
            }
        }catch(IOException e){
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }
    }


   public static LinkedList<String> findKeyRecursive(JSONObject currObject, String requireKey, LinkedList<String> ansList) {
       currObject.keySet().forEach(key -> {
           Object value = currObject.get(key);
           // If value is JSONobject we sould get deeper in the recursion
           if (value instanceof JSONObject) {
               ansList.addAll(findKeyRecursive((JSONObject) value, requireKey,  new LinkedList<>()));
           }else if (key.toString().equals(requireKey)) {
               // else if key equals to requireKey and value is string we add it to the list.
               if(value instanceof String && !ansList.contains(value.toString())){
                   ansList.add(value.toString());
               // or if valie is JSONarray we should add each value.
               }else if(value instanceof JSONArray){
                   for (Object val: (JSONArray)value){
                        if(val instanceof String && !ansList.contains(val.toString()))
                            ansList.add(val.toString());
                   }
               }
           }
       });
       return ansList;
   }

}

