package fullTest1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;

public class Favorite_word_manager {
	public static void addNewFavWord(String word, String definition) {
	    try {
	        List<String> words = Files.readAllLines(Paths.get("fav_words.txt"));
	        if (!words.contains(word + "|" + definition)) {
	            try (BufferedWriter writer = new BufferedWriter(new FileWriter("fav_words.txt", true))) {
	                writer.write(word +"|"+definition);
	                writer.newLine();
	                System.out.println("FWM: added: " + word + "|" + definition);
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void removeFavWord(String word, String def) {
	    try {
	        List<String> words = Files.readAllLines(Paths.get("fav_words.txt"));
	        words.removeIf(w -> w.equalsIgnoreCase(word+"|"+def));
	        Files.write(Paths.get("fav_words.txt"), words, StandardOpenOption.TRUNCATE_EXISTING);
	        System.out.println("FWM: deleted: " + word );
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static boolean checkFavWordExist(String word, String def) {
	    try {
	        List<String> words = Files.readAllLines(Paths.get("fav_words.txt"));
	        String target = word +"|" + def;
	        return words.contains(target);
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public static String[] listFavWord() {
		 List<String> words = new ArrayList<>();
	        try (BufferedReader reader = new BufferedReader(new FileReader("fav_words.txt"))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                // Split line into word and definition
	                String[] parts = line.split("\\|", 2); // limit=2 keeps definition intact
	                if (parts.length >= 1) {
	                    String word = parts[0].trim();
	                    if (!word.isEmpty()) {
	                        words.add(word);
	                    }
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        // Convert List<String> to String[]
	        return words.toArray(new String[0]);

	}
	
	public static void resetFavWord() {
		try {
	        Files.write(Paths.get("fav_words.txt"), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
	        System.out.println("FWM: deleted: all_word_in_file");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	}
	
	public static String getWordDefinition(String word) {
		try (BufferedReader reader = new BufferedReader(new FileReader("fav_words.txt"))){
			String line;
			while((line = reader.readLine()) != null) {
				String[] parts = line.split("\\|", 2);
				if(parts.length == 2) {
					String fileWord = parts[0].trim();
					String defintion = parts[1].trim();
					
					if(fileWord.equalsIgnoreCase(word)) {
						return defintion;
					}
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String[] listFavWordAndDefinitions() {
		 List<String> wordsAndDefinitions = new ArrayList<>();
	        try (BufferedReader reader = new BufferedReader(new FileReader("fav_words.txt"))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                wordsAndDefinitions.add(line);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        // Convert List<String> to String[]
	        return wordsAndDefinitions.toArray(new String[0]);

	}
	
	public static String[] separateWordAndDefinition(String n) {
		String original = n;
		String[] parts = original.split("\\|", 2);
		return parts;
	}
	
	public static void main(String[] args) {
//		List<String> words = new ArrayList<>();
//		List<String> definitions = new ArrayList<>();
//		String[] n = listFavWordAndDefinitions();
//		for(int i = 0; i < n.length; i++) {
//			String[] m = separateWordAndDefinition(n[i]);
//			for(int x = 0; x < m.length; x++) {
//				if(x == 0) {
//					words.add(m[x]);
//				} else {
//					definitions.add(m[x]);
//				}
//			}
//		}
//		
//		System.out.println(Arrays.toString(words.toArray()));
//		System.out.println(Arrays.toString(definitions.toArray()));
		
//		resetFavWord();
//		System.out.print(checkFavWordExist("mother", "Người đàn bà có con, trong quan hệ với con cái"));
	}

}
