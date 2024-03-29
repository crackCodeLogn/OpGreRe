package com.vv.personal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.*;
import java.util.List;

/**
 * @author Vivek
 * @since 2024-01-23
 */
@Getter
@AllArgsConstructor
public class WordModel {

    private final String word;
    private final List<String> wordMeaning;
    private final ImageIcon image;

    @Override
    public String toString() {
        return String.format("%s: %s, %d", word, wordMeaning.subList(0, Math.min(2, wordMeaning.size())), image.getImageLoadStatus());
    }
}