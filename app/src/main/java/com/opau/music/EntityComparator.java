package com.opau.music;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class EntityComparator implements Comparator<Entity> {
    Collator spCollator = Collator.getInstance(Locale.getDefault());
    public int compare (Entity e1, Entity e2){
        return spCollator.compare(e1.getLabel(), e2.getLabel());
    }
}