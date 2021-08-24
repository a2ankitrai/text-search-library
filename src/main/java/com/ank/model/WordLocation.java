package com.ank.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class WordLocation {

    UUID fileId;
    int position;

}
