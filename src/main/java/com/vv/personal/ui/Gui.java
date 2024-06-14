package com.vv.personal.ui;

import com.vv.personal.model.UiMode;
import com.vv.personal.model.WordModel;
import com.vv.personal.util.LoggingHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Vivek
 * @version 1.0
 * @since 03-12-2017
 * revived on 2024-01-23
 */
public class Gui extends JFrame implements ActionListener {
    private static final String UNCHECKED = "UNCHECKED";
    private static final String CHECKED = "CHECKED";
    private static final String ACTION_COMMAND_CHECKED = "-1";
    private static final String ACTION_COMMAND_UNCHECKED = "-2";

    private final List<WordModel> wordModelList;
    private final TreeSet<String> markedWordsForLaterPractise;
    private final JLabel targetWordLabel;
    private final JLabel targetWordImageLabel;
    private final JTextArea targetWordMeaningText;
    private final JButton buttonChecked;
    private String currentWordBeingLookedAt;

    public Gui(List<WordModel> wordModelList, UiMode uiMode, TreeSet<String> markedWordsForLaterPractise) {
        this.wordModelList = wordModelList;
        this.markedWordsForLaterPractise = markedWordsForLaterPractise;

        int n = wordModelList.size();
        JButton[] buttonList = new JButton[n];

        setLayout(new BorderLayout());
        JPanel panel1 = new JPanel(new GridLayout(n, 1));

        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane.setResizeWeight(.3);
        jSplitPane.setEnabled(false);

        JScrollPane jScrollPane = new JScrollPane(panel1);
        jScrollPane.createVerticalScrollBar();
        jScrollPane.getVerticalScrollBar().setUnitIncrement(25);

        for (int i = 0; i < n; i++) {
            String word = wordModelList.get(i).getWord();
            String buttonText = String.format("%d. %s%s", i + 1, Character.toUpperCase(word.charAt(0)), word.substring(1));
            buttonList[i] = new JButton(buttonText);
            buttonList[i].addActionListener(this);
            buttonList[i].setActionCommand(String.valueOf(i));
            panel1.add(buttonList[i]);
        }

        JPanel panel2 = new JPanel(new GridLayout(3, 1));

        JPanel p21 = new JPanel();
        String firstWord = wordModelList.get(0).getWord();
        targetWordLabel = new JLabel("1. " + firstWord.toUpperCase());
        currentWordBeingLookedAt = firstWord;
        targetWordLabel.setFont(targetWordLabel.getFont().deriveFont(36f));

        buttonChecked = new JButton(UNCHECKED);
        buttonChecked.addActionListener(this);
        buttonChecked.setActionCommand("-1");
        p21.add(targetWordLabel);
        p21.add(Box.createHorizontalStrut(100));
        p21.add(buttonChecked); // allowing marking in all modes

        JPanel p22 = new JPanel();
        targetWordImageLabel = new JLabel();
        ImageIcon image = wordModelList.get(0).getImage();
        targetWordImageLabel.setIcon(image);
        p22.add(targetWordImageLabel);

        JPanel p23 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String wordMeaningText = generateWordMeaningText(wordModelList.get(0).getWordMeaning());
        targetWordMeaningText = new JTextArea(wordMeaningText);
        targetWordMeaningText.setFont(targetWordMeaningText.getFont().deriveFont(17f));

        p23.add(targetWordMeaningText);
        p23.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JScrollPane jScrollPane1 = new JScrollPane(p23);
        jScrollPane1.createHorizontalScrollBar();

        panel2.add(p21);
        panel2.add(p22);
        panel2.add(jScrollPane1);

        jSplitPane.add(jScrollPane);
        jSplitPane.add(panel2);

        add(jSplitPane, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = Integer.parseInt(e.getActionCommand());
        if (index >= 0) { // clicked on the left row of words
            targetWordLabel.setText(generateWordLabel(index + 1, wordModelList.get(index).getWord()));

            ImageIcon image = wordModelList.get(index).getImage();
            targetWordImageLabel.setIcon(image);

            String wordMeaningText = generateWordMeaningText(wordModelList.get(index).getWordMeaning());
            targetWordMeaningText.setText(wordMeaningText);
            targetWordMeaningText.setFont(targetWordMeaningText.getFont().deriveFont(17f));
            currentWordBeingLookedAt = wordModelList.get(index).getWord();

            if (!markedWordsForLaterPractise.contains(currentWordBeingLookedAt)) {
                buttonChecked.setText(UNCHECKED);
                buttonChecked.setActionCommand(ACTION_COMMAND_CHECKED);
            } else {
                buttonChecked.setActionCommand(ACTION_COMMAND_UNCHECKED);
                buttonChecked.setText(CHECKED);
            }
        } else if (index == -1) { // word marked
            markedWordsForLaterPractise.add(currentWordBeingLookedAt);
            buttonChecked.setActionCommand(ACTION_COMMAND_UNCHECKED);
            buttonChecked.setText(CHECKED);

            LoggingHelper.info(String.format("%d, current word list: %s", markedWordsForLaterPractise.size(), markedWordsForLaterPractise));
        } else if (index == -2) { // word unmarked
            markedWordsForLaterPractise.remove(currentWordBeingLookedAt);
            buttonChecked.setActionCommand(ACTION_COMMAND_CHECKED);
            buttonChecked.setText(UNCHECKED);

            LoggingHelper.info(String.format("%d, current word list: %s", markedWordsForLaterPractise.size(), markedWordsForLaterPractise));
        }
    }

    private String generateWordLabel(int index, String word) {
        return String.format("%d. %s", index, word.toUpperCase());
    }

    private String generateWordMeaningText(List<String> meanings) {
        StringBuilder targetAreaText = new StringBuilder();
        meanings.forEach(meaning -> targetAreaText.append(String.format("-> %s\n", meaning)));
        return targetAreaText.toString();
    }
}
