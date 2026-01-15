package fullTest1;
import static fullTest1.Favorite_word_manager.listFavWordAndDefinitions;
import static fullTest1.Favorite_word_manager.separateWordAndDefinition;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.*;

@SuppressWarnings("serial")
public class MyFrame extends JFrame implements ActionListener, KeyListener {

    JPanel p1, p2, sp1, sp2;
    JButton b1, b2, b3;
    JTextField tf1;
    JLabel l1, l2;
    JScrollPane scrollPane;
    ImageIcon vie, eng, liked, search, switchLang;
    int mode = 1; //1-> vieToEng    2-> engToVie

    Database_Manager dbm = new Database_Manager();

    JPopupMenu popup = new JPopupMenu();
    DefaultListModel<String> model = new DefaultListModel<>();
    JList<String> suggestionList = new JList<>(model);

    MyFrame() throws SQLException {
//    	vie = new ImageIcon("viePlaceHolder2.png");
//        eng = new ImageIcon("engPlaceHolder2.png");
        liked = new ImageIcon("likedWordsButtonPlaceHolder.png");
        search = new ImageIcon("searchButtonPlaceHolder.png");
        switchLang = new ImageIcon("switchLanguagePlaceHolder.png");
        
//        Image vieImg = vie.getImage();
//        Image engImg = eng.getImage();
        Image likedImg = liked.getImage();
        Image searchImg = search.getImage();
        Image switchImg = switchLang.getImage();

//        Image scaledVie = vieImg.getScaledInstance(112, 60, Image.SCALE_SMOOTH);
//        Image scaledEng = engImg.getScaledInstance(112, 60, Image.SCALE_SMOOTH);
        Image scaledLiked = likedImg.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        Image scaledSearch = searchImg.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        Image scaledSwitch = switchImg.getScaledInstance(112, 60, Image.SCALE_SMOOTH);

//        ImageIcon scaledVieIcon = new ImageIcon(scaledVie);
//        ImageIcon scaledEngIcon = new ImageIcon(scaledEng);
        ImageIcon scaledLikedIcon = new ImageIcon(scaledLiked);
        ImageIcon scaledSearchIcon = new ImageIcon(scaledSearch);
        ImageIcon scaledSwitchIcon = new ImageIcon(scaledSwitch);

        
        p1 = new JPanel();
        p2 = new JPanel();
        sp1 = new JPanel();
        sp2 = new JPanel();

        b1 = new JButton("📑");
        b2 = new JButton("🔎");
        b3 = new JButton("🔁");

        tf1 = new JTextField();

        l1 = new JLabel("VIE");
        l2 = new JLabel("ENG");
        
        

        this.setLayout(new BorderLayout(0, 0));
        p1.setLayout(new GridLayout(2, 1, 0, 0));
        sp1.setLayout(new FlowLayout());
        sp2.setLayout(new GridLayout(1, 3, 0, 0));

        sp1.setBackground(Color.gray);
        sp2.setBackground(Color.white);

        p1.setPreferredSize(new Dimension(350, 120));
        sp1.setPreferredSize(new Dimension(350, 60));
        sp2.setPreferredSize(new Dimension(350, 60));
        tf1.setPreferredSize(new Dimension(200, 50));

        b1.setPreferredSize(new Dimension(50, 50));
        b2.setPreferredSize(new Dimension(50, 50));
        b3.setPreferredSize(new Dimension(75, 50));

        b1.setFocusable(false);
        b2.setFocusable(false);
        b3.setFocusable(false);

        b1.addActionListener(this);
        b2.addActionListener(this);
        b3.addActionListener(this);
        tf1.addKeyListener(this);

        l1.setHorizontalAlignment(JLabel.CENTER);
        l2.setHorizontalAlignment(JLabel.CENTER);
        tf1.setFont(new Font("", Font.PLAIN, 20));

        l1.setFont(new Font("", Font.PLAIN, 40));
        l2.setFont(new Font("", Font.PLAIN, 40));
        b1.setFont(new Font("", Font.PLAIN, 16));
        b2.setFont(new Font("", Font.PLAIN, 16));
        b3.setFont(new Font("", Font.PLAIN, 40));


        sp1.add(b1);
        sp1.add(tf1);
        sp1.add(b2);

        sp2.add(l1);
        sp2.add(b3);
        sp2.add(l2);

        p1.add(sp1);
        p1.add(sp2);

        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(p2,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        this.add(p1, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Eng-Vie Dictionary");
        this.setResizable(false);
        this.setSize(350, 490);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setAlwaysOnTop(false);
        
        System.out.println("fav: " + b1.getSize());
        System.out.println("src: " + b2.getSize());
        System.out.println("change: " + b3.getSize());
        System.out.println("vie: " + l1.getSize());
        System.out.println("eng: " + l2.getSize());



        //finish basics

        //autocomplete suggestions
        popup.setFocusable(false);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //select 1 item at a time
        JScrollPane suggestionScroll = new JScrollPane(suggestionList);
        suggestionScroll.setBorder(null);
        popup.add(suggestionScroll);

 
        suggestionList.addMouseListener(new MouseAdapter() { //set up directly
            @Override
            public void mouseClicked(MouseEvent e) {
                String selected = suggestionList.getSelectedValue();
                if (selected != null) {
                    tf1.setText(selected); //set textfield to selected text
                    popup.setVisible(false); //hide suggestion
                    button2();  //search
                }
            }
        });

        tf1.getDocument().addDocumentListener(new DocumentListener() {
            private void updatePopup() {
                model.clear(); //when tf1's content changes, the suggestion list is refreshed
                String input = tf1.getText().toLowerCase();
                String normalizedInput = removeAccents(input); //turn inputed words to plain, accent-less words (mẹ -> me)

                if (input.isEmpty()) {
                    popup.setVisible(false);
                    return; //is tf1 is empty, hide pop-up
                }

                try {
                    String[] dictionary = dbm.listAllWord(mode);  //list all words available in searching language
                    for (String word : dictionary) {
                        String normalizedWord = removeAccents(word.toLowerCase()); //db words might be capped and/or have accents -> remove accents
 
                        if (normalizedWord.startsWith(normalizedInput)) { //if the accent-less db word list's word starts with or is the accent-less input
                            model.addElement(word); //add the word to the suggestion list
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (!model.isEmpty()) {
                    suggestionList.setVisibleRowCount(Math.min(model.size(), 5)); //limit the visible length of the list, if not, the pop up would be as long as the suggestion list
                    popup.show(tf1, 0, tf1.getHeight()); //display at tf1, shifted x by 0 and y by exactly the height of tf1 -> just below tf1
                } else {
                    popup.setVisible(false);
                }
            }

            @Override public void insertUpdate(DocumentEvent e) { updatePopup(); } //update the pop up when a character is typed  (insert)
            @Override public void removeUpdate(DocumentEvent e) { updatePopup(); } //update the pop up when a character is removed
            @Override public void changedUpdate(DocumentEvent e) { updatePopup(); } //update the pop up when any change happened
        });
    }


    public static String removeAccents(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD); //NFD: Normalization Form Decomposition ToT -> decompose input into its base components (letters, accents, other marks, etc). ex: ố -> o + ^ + '    
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");  //set all the accent marks in input into a regex
        return pattern.matcher(normalized).replaceAll(""); //remove all accent marks in input
    }
    
    //Pattern.complie to make regex
    //"\\p{InCombiningDiacriticalMarks" to choose only accent marks
    
    public void button3() { //change translation mode (eng to vie/ vie to eng) 
    	if (l1.getText().equals("VIE")) {
            mode = 2;
            l1.setText("ENG");
            l2.setText("VIE");
            l1.setFont(new Font("", Font.PLAIN, 40));
            l2.setFont(new Font("", Font.PLAIN, 40));
        } else if (l1.getText().equals("ENG")) {
            mode = 1;
            l1.setText("VIE");
            l2.setText("ENG");
            l1.setFont(new Font("", Font.PLAIN, 40));
            l2.setFont(new Font("", Font.PLAIN, 40));
        }
    }
    
    public void button2() { //search button
    	String[] words = null;
        int a = 0;
    	
		if(!(tf1.getText().isEmpty())) {
			try {
//				words = dbm.searchForWord(tf1.getText(), mode);
				words = dbm.searchForWord(tf1.getText().toLowerCase().trim(), mode);  //get list of words

				a = words.length;
				
			} catch(SQLException _) {
                JOptionPane.showMessageDialog(this, "sql error");

			}
		} 
 	
        p2.removeAll();
        
        if(a == 0) { //if the list is empty, don't show the list but show "no result" label
        	JLabel noResult = new JLabel();
        	JPanel panel = new JPanel();
        	noResult.setText("no result");
            panel.add(noResult, BorderLayout.WEST);
        	p2.add(panel);
        	
        } else {
        	
        	for (int i = 1; i <= a; i++) {
        	    JButton panel = new JButton();
        	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        	    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 

        	    JLabel resultWord = new JLabel(words[i - 1]);
        	    resultWord.setFont(new Font("", Font.BOLD, 16));
        	    resultWord.setAlignmentX(Component.LEFT_ALIGNMENT); 

        	    String engWord = null;
        	    String vieWord = null;

        	    if (mode ==1) { //vie to eng
        	        vieWord =tf1.getText().toLowerCase().trim();	//set vie or eng word
        	        engWord =words[i-1];
        	    } else if (mode == 2) { //eng to vie
        	        engWord =tf1.getText().toLowerCase().trim();
        	        vieWord =words[i-1];
        	    }

        	    JLabel definitionLabel = new JLabel();
        	    final String[] definition = {null};
        	    int defMaxLength = 42; // <- nice

        	    try {
        	        String defText = dbm.searchForDefinition(engWord, vieWord, mode); //get word's definition
        	        if (defText != null && defText.length() > defMaxLength) {
        	            definition[0] = defText;
        	            panel.setToolTipText(defText);
        	            defText = defText.substring(0, defMaxLength - 3) + "...";
        	        } else {
        	            definition[0] = defText; // store even if shorter
        	        }
        	        definitionLabel.setText(defText);
        	    } catch (SQLException _) {
        	        JOptionPane.showMessageDialog(this, "sql error");
        	    }
        	    definitionLabel.setFont(new Font("", Font.PLAIN, 14));
        	    definitionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        	    
        	    panel.add(resultWord);
        	    panel.add(Box.createVerticalStrut(3));
        	    panel.add(definitionLabel);


        	    panel.setPreferredSize(new Dimension(300, 70));
        	    panel.setMaximumSize(new Dimension(300, 70));
        	    panel.setMinimumSize(new Dimension(300, 70));
        	    panel.setBackground(i % 2 ==0 ? Color.LIGHT_GRAY : Color.WHITE); //switch between light gray and white for the result panels

        	    panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        	    
        	    p2.add(panel);
        	    p2.add(Box.createVerticalStrut(10)); 
        	    
//        	    panel.addActionListener(this);
        	    
        	    panel.addActionListener(_ -> {
        	        JFrame detail = new JFrame();
        	        detail.setSize(500,200);
        	        detail.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        	        detail.setVisible(true);
        	        detail.setLocationRelativeTo(this);
        	        
        	        JToggleButton tb1 = new JToggleButton("like");
        	        JLabel word = new JLabel();
        	        word.setHorizontalAlignment(SwingConstants.CENTER);
        	        word.setVerticalAlignment(SwingConstants.CENTER);
        	        JLabel def = new JLabel();

        	        JLabel pronunciation1 = new JLabel();
        	        JLabel pronunciation2 = new JLabel();
        	        
        	        JPanel dp1, dp2, dsp1;
        	        dp1 = new JPanel();
        	        dp2 = new JPanel();
        	        dsp1 = new JPanel();
        	        
        	        tb1.setFocusable(false);
        	        
        	        if(Favorite_word_manager.checkFavWordExist(resultWord.getText(), definition[0])) {
            	        tb1.setSelected(true);
        	        } else {
            	        tb1.setSelected(false);
        	        }
        	        
        	        String[] pron = dbm.getPronunciation(resultWord.getText());
        	        pronunciation1.setText(pron[0]);
        	        pronunciation2.setText(pron[1]);
        	        
        	        
        	        word.setOpaque(true);
        	        pronunciation1.setOpaque(true);
        	        pronunciation2.setOpaque(true);
        	        
        	        word.setBackground(Color.white);
        	        pronunciation1.setBackground(Color.LIGHT_GRAY);
        	        dp2.setBackground(Color.LIGHT_GRAY);

        	        if (pronunciation2.getText().isEmpty()){
            	        pronunciation2.setBackground(Color.LIGHT_GRAY);
        	        }else {
        	        	pronunciation2.setBackground(Color.gray);
        	        }
        	        
//        	        tb1.setSelected(false);
        	        word.setText(resultWord.getText());
        	        def.setText(definition[0]);        	        
        	        detail.setLayout(new GridLayout(2,1));
        	        dp1.setLayout(new GridLayout(1,3));
        	        dp2.setLayout(new BorderLayout());
        	        dsp1.setLayout(new GridLayout(2,1));
        	        dsp1.add(pronunciation1);
        	        dsp1.add(pronunciation2);
        	        dp1.add(word);
        	        dp1.add(dsp1);
        	        dp1.add(tb1);
        	       
        	        
        	        dp2.add(def, BorderLayout.WEST);

        	        detail.add(dp1);
        	        detail.add(dp2);
        	        

        	        tb1.addActionListener(_ ->{
        	        	if((tb1.isSelected())){
        	        		Favorite_word_manager.addNewFavWord(resultWord.getText(), def.getText());
        	        		System.out.println("added new fav word");
        	        	} else {
        	        		Favorite_word_manager.removeFavWord(resultWord.getText(), def.getText());
        	        		System.out.println("remove fav word");

        	        	}
        	        	
        	        }); 
        	        
        	    });

        	}

        }
      
        
        p2.revalidate();
        p2.repaint();
    }
    
    public void button1() { //favorite words button
    	List<String> wordsList = new ArrayList<>();
		List<String> definitionsList = new ArrayList<>();
		String[] n = listFavWordAndDefinitions();
		for(int i = 0; i < n.length; i++) {
			String[] m = separateWordAndDefinition(n[i]);
			for(int x = 0; x < m.length; x++) {
				if(x == 0) {
					wordsList.add(m[x]);
				} else {
					definitionsList.add(m[x]);
				}
			}
		}
    	
    	String[] words = wordsList.toArray(new String[0]);
    	String[] definitions = definitionsList.toArray(new String[0]);
    	
    	int a = words.length;
    	
    	 p2.removeAll();
         
         if(a == 0) {
         	JLabel noResult = new JLabel();
         	JPanel panel = new JPanel();
         	noResult.setText("no result");
             panel.add(noResult, BorderLayout.WEST);
         	p2.add(panel);
         	
         } else {
         	
         	for (int i = 1; i <= a; i++) {
         	    JButton panel = new JButton();
         	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
         	    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 

         	    JLabel resultWord = new JLabel(words[i - 1]);
         	    resultWord.setFont(new Font("", Font.BOLD, 16));
         	    resultWord.setAlignmentX(Component.LEFT_ALIGNMENT); 

         	    JLabel definitionLabel = new JLabel();
        	    final String[] definition = {null};
	       	    int defMaxLength = 42;   	    
	       	    
	       	    String defText = definitions[i - 1];	//get word's definition
	       	    if (defText != null) {
     	            definition[0] = defText;
	       	    	if(defText.length() > defMaxLength) {
		       	    	panel.setToolTipText(defText);
		       	    	defText = defText.substring(0, defMaxLength - 3) + "...";
	       	    	}
	       	    }
	       	    definitionLabel.setText(defText);
	       	   
	       	    
	       	    definitionLabel.setFont(new Font("", Font.PLAIN, 14));
	       	    definitionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

         	    

         	    panel.add(resultWord);
         	    panel.add(Box.createVerticalStrut(3));
        	    panel.add(definitionLabel);


         	    panel.setPreferredSize(new Dimension(300, 70));
         	    panel.setMaximumSize(new Dimension(300, 70));
         	    panel.setMinimumSize(new Dimension(300, 70));
         	    panel.setBackground(i % 2 ==0 ? Color.LIGHT_GRAY : Color.WHITE);

         	    panel.setAlignmentX(Component.CENTER_ALIGNMENT);
         	    
         	    p2.add(panel);
         	    p2.add(Box.createVerticalStrut(10)); 
         	    
         	    
         	    panel.addActionListener(this);

        	    panel.addActionListener(_ -> {
        	        JFrame detail = new JFrame();
        	        detail.setSize(500,200);
        	        detail.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        	        detail.setVisible(true);
        	        detail.setLocationRelativeTo(this);
        	        
        	        JToggleButton tb1 = new JToggleButton("like");
        	        JLabel word = new JLabel();
        	        word.setHorizontalAlignment(SwingConstants.CENTER);
        	        word.setVerticalAlignment(SwingConstants.CENTER);
        	        JLabel def = new JLabel();
        	        JLabel pronunciation1 = new JLabel();
        	        JLabel pronunciation2 = new JLabel();
        	        
        	        JPanel dp1,dp2, dsp1;
        	        dp1 = new JPanel();
        	        dp2 = new JPanel();
        	        dsp1 = new JPanel();
        	        
        	        if(Favorite_word_manager.checkFavWordExist(resultWord.getText(), definition[0])) {
            	        tb1.setSelected(true);
        	        } else {
            	        tb1.setSelected(false);
        	        }
        	        
        	        String[] pron = dbm.getPronunciation(resultWord.getText());
        	        pronunciation1.setText(pron[0]);
        	        pronunciation2.setText(pron[1]);
        	        
        	        word.setOpaque(true);
        	        pronunciation1.setOpaque(true);
        	        pronunciation2.setOpaque(true);
        	        
        	        word.setBackground(Color.white);
        	        pronunciation1.setBackground(Color.LIGHT_GRAY);
        	        dp2.setBackground(Color.LIGHT_GRAY);

        	        if (pronunciation2.getText().isEmpty()){
            	        pronunciation2.setBackground(Color.LIGHT_GRAY);
        	        }else {
        	        	pronunciation2.setBackground(Color.gray);
        	        }
        	        
        	        tb1.setFocusable(false);
        	        
        	        tb1.setSelected(true);
        	        word.setText(resultWord.getText());
        	        def.setText(definition[0]);
        	        detail.setLayout(new GridLayout(2,1)); 
        	        dp1.setLayout(new GridLayout(1,3));
        	        dp2.setLayout(new BorderLayout());
        	        dsp1.setLayout(new GridLayout(2,1));
        	        dsp1.add(pronunciation1);
        	        dsp1.add(pronunciation2);
        	        dp1.add(word);
        	        dp1.add(dsp1);
        	        dp1.add(tb1);
        	        dp2.add(def);
        	        detail.add(dp1);
        	        detail.add(dp2);
        	        
        	        tb1.addActionListener(_ ->{
        	        	if((tb1.isSelected())){
        	        		Favorite_word_manager.addNewFavWord(resultWord.getText(), def.getText());
//        	        		System.out.println("MyFrame: added new fav word");
        	        	} else {
        	        		Favorite_word_manager.removeFavWord(resultWord.getText(), def.getText());
//        	        		System.out.println("MyFrame: remove fav word");

        	        	}
        	        	
        	        }); 
        	        
        	    });
         	}
         }
         p2.revalidate();
         p2.repaint();
    }
    

    @Override
    public void actionPerformed(ActionEvent e){
        if (e.getSource() == b3) { //switch mode
            button3();
        }

        if (e.getSource() == b2) { //search for word
        	button2();
        }
        
        if(e.getSource() == b1) { //list favorite words
        	button1();
        }

    
    }

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_ENTER) {
			button2();
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
