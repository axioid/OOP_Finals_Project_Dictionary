package fullTest1;

import static fullTest1.Favorite_word_manager.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Arrays;
import java.util.List;

public class Database_Manager {
	
	private Connection conn;
	
	public Database_Manager() throws SQLException{
		conn = DriverManager.getConnection("jdbc:sqlite:dictionary_test3.db");
	}
	
	public boolean engWordExists(String word) throws SQLException{
		String sql = "SELECT word FROM EnglishNew WHERE word = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, word);
			
			try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                int count = rs.getInt(1);	
	                return count > 0;
	            }
	        }
		}
		return false;	
	}
	
	public boolean vieWordExists(String word) throws SQLException{
		String sql = "SELECT word FROM VietnameseNew WHERE word = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, word);
			
			try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                int count = rs.getInt(1);	
	                return count > 0;
	            }
	        }
		}
		return false;	
	}
	
	public String[] searchForWord(String word, int mode) throws SQLException {
	    String sql = null;
	    if (mode == 1) {
	        sql = "SELECT e.word " +
	              "FROM EngVie_Map m " +
	              "JOIN VietnameseNew v ON m.vie_id = v.vie_id " +
	              "JOIN EnglishNew e ON m.eng_id = e.eng_id " +
	              "WHERE v.word = ?";
	    } else if (mode == 2) {
	        sql = "SELECT v.word " +
	              "FROM EngVie_Map m " +
	              "JOIN EnglishNew e ON m.eng_id = e.eng_id " +
	              "JOIN VietnameseNew v ON m.vie_id = v.vie_id " +
	              "WHERE e.word = ?";
	    } 

	    List<String> results = new ArrayList<>();

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setString(1, word);
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                results.add(rs.getString("word"));
	            }
	        }
	    }

	    return results.toArray(new String[0]);
	}

	public String searchForDefinition(String engWord, String vieWord, int mode ) throws SQLException{
		String sqlCode = null;
		if (mode ==1) {
			sqlCode = "SELECT DefinitionForEng FROM EngVie_Map "
					+ "JOIN EnglishNew ON EngVie_Map.eng_id = EnglishNew.eng_id "
					+ "JOIN VietnameseNew ON EngVie_Map.vie_id = VietnameseNew.vie_id "
					+ "WHERE EnglishNew.word = ? AND VietnameseNew.word = ?";
		} else if(mode==2) {
			sqlCode = "SELECT DefinitionForVie FROM EngVie_Map "
					+ "JOIN EnglishNew ON EngVie_Map.eng_id = EnglishNew.eng_id "
					+ "JOIN VietnameseNew ON EngVie_Map.vie_id = VietnameseNew.vie_id "
					+ "WHERE EnglishNew.word = ? AND VietnameseNew.word = ?";
		}
		
		try(PreparedStatement ps = conn.prepareStatement(sqlCode)){
			ps.setString(1, engWord);
			ps.setString(2, vieWord);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					return rs.getString(1);
				}
			}
			
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String[] listAllWord(int mode) {
		String sqlCode = null;
		if(mode ==1) {
			sqlCode = "SELECT word FROM VietnameseNew";
		} else if (mode == 2) {
			sqlCode = "SELECT word FROM EnglishNew";
		}
	    List<String> results = new ArrayList<>();
	    try (PreparedStatement ps = conn.prepareStatement(sqlCode);
	         ResultSet rs = ps.executeQuery()) {
	        while (rs.next()) {
	            results.add(rs.getString("word"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return results.toArray(new String[0]);
	}
	
	public String[] listAllWord() {
		String sqlCode = "SELECT word FROM EnglishNew UNION SELECT word FROM VietnameseNew";
	    List<String> results = new ArrayList<>();
	    try (PreparedStatement ps = conn.prepareStatement(sqlCode);
	         ResultSet rs = ps.executeQuery()) {
	        while (rs.next()) {
	            results.add(rs.getString("word"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return results.toArray(new String[0]);
	}
	
	public String[] getPronunciation(String word) {
//		List<String> pronunciation = new ArrayList<>();
		
		String sql1 = "Select pronounciation FROM VietnameseNew WHERE word = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql1)){
			ps.setString(1, word);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					String vn = rs.getString("pronounciation");
					return new String[] {vn, ""};
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		String sql2 = "Select pronounciationUS, pronounciationUK FROM EnglishNew WHERE word = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql2)){
			ps.setString(1, word);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					String uk = rs.getString("pronounciationUK");
					String us = rs.getString("pronounciationUS");
					
					return new String[] {"US: " + us, "UK: "+ uk};
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return new String[0];
	}
	
	public static void main(String[] args) throws SQLException{
		Database_Manager dbm = new Database_Manager();
		System.out.println(Arrays.toString(listFavWord()));
		for(String i : listFavWord()) {
			String[] pron = dbm.getPronunciation(i);
			for(int n = 0; n < pron.length; n++) {
				System.out.println(pron[n]);
			}
		}
	}
}
