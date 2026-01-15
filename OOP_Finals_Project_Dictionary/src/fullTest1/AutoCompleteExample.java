package fullTest1;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class AutoCompleteExample {
    
    // Helper method to strip accents
    public static String removeAccents(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    public static void main(String[] args) throws SQLException {
        
        Database_Manager dbm = new Database_Manager();
        
        JFrame frame = new JFrame("Autocomplete");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(null);

        JTextField textField = new JTextField();
        textField.setBounds(50, 50, 200, 30);

        String[] dictionary = dbm.listAllWord(1);

        JPopupMenu popup = new JPopupMenu();
        popup.setFocusable(false); // don't steal focus

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> suggestionList = new JList<>(model);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Put the list inside a scroll pane
        JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setBorder(null);
        popup.add(scrollPane);

        // Handle mouse clicks on suggestions
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selected = suggestionList.getSelectedValue();
                if (selected != null) {
                    textField.setText(selected);
                    popup.setVisible(false);
                }
            }
        });

        // Update suggestions as user types
        textField.getDocument().addDocumentListener(new DocumentListener() {
            private void updatePopup() {
                model.clear();
                String input = textField.getText().toLowerCase();
                String normalizedInput = removeAccents(input);

                if (input.isEmpty()) {
                    popup.setVisible(false);
                    return;
                }

                for (String word : dictionary) {
                    String normalizedWord = removeAccents(word.toLowerCase());

                    // Match by accent-stripped comparison
                    if (normalizedWord.startsWith(normalizedInput)) {
                        model.addElement(word); // show original word with accents
                    }
                }

                if (!model.isEmpty()) {
                    suggestionList.setVisibleRowCount(Math.min(model.size(), 5));
                    popup.show(textField, 0, textField.getHeight());
                } else {
                    popup.setVisible(false);
                }
            }

            @Override public void insertUpdate(DocumentEvent e) { updatePopup(); }
            @Override public void removeUpdate(DocumentEvent e) { updatePopup(); }
            @Override public void changedUpdate(DocumentEvent e) { updatePopup(); }
        });

        frame.add(textField);
        frame.setVisible(true);
    }
}