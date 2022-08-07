package com.jmonkeyengine.jmeinitializer.libraries;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Categories are used to group what is offered by the UI. Sometimes a group will only allow 1 item to be selected
 * within it (e.g. it only makes sense to have 1 physics engine)
 */
@AllArgsConstructor
@Getter
public enum LibraryCategory {


    /**
     * Contains the targeted platform (Desktop, VR, android etc)
     */
    JME_PLATFORM( "JME Platform", "JME can target many platforms, select the platform your game will target", false),

    /**
     * JME libraries that don't fit into other categories
     */
    JME_GENERAL( "Other JME libraries", "These are general purpose libaries that may be useful in JME games",false),


    PHYSICS( "Physics Libraries", "A physics library handles collisions and forces like gravity", true),

    GUI( "GUI libraries", "A Gui library will provide the 2D interface over your 3D world",  false),

    NETWORKING( "Networking", "A Networking library will help with multiplayer games",  false),

    ASSETS("Assets", "Asset bundles available to add content within applications", false),

    MATERIALS_AND_EFFECTS("Materials & Effects", "Libraries for effects like explosions or smoke & materials", false),

    TERRAIN("Terrain", "Libraries supporting the rendering or generation of Terrain", false),

    GENERAL("Other Community Libraries", "Libraries provided by the JME community", false),

    HIDDEN("Hidden", "A technical category that should never make it to the UI", false)
    ;

    String displayName;

    /**
     * Used in the UI to describe the general purpose of the category
     */
    String description;

    /**
     * If true a radio selector will be presented in the UI. If false checkboxes are presented.
     *
     * NOT ACTUALLY CURRENTLY RESPECTED BY THE UI. Currently, some categories are manually set as checkbox in the UI and
     * the rest are radio
     */
    boolean onlyOneAllowed;
}
