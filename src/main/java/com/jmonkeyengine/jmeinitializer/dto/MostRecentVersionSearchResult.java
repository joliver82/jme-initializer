package com.jmonkeyengine.jmeinitializer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

@Data
@AllArgsConstructor
public class MostRecentVersionSearchResult{
    boolean found;
    String version;
}
