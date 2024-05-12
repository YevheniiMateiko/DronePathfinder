package com.example.dronepathfinder.objects;

import java.io.Serializable;

public class Object implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private boolean pinned;

    Object(String name)
    {
        this.name = name;
        this.pinned = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void changePinned()
    {
        this.pinned = !this.pinned;
    }
}
