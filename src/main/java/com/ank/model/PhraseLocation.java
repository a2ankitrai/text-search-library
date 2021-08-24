package com.ank.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhraseLocation {

    String fileName;
    int position;

    public String toString() {
        return "[File Name: " + fileName + ", Position: " + position + "]";
    }
}
