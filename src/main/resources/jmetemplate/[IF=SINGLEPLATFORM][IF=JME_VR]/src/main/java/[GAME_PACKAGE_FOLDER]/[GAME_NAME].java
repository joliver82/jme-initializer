package [GAME_PACKAGE];

import com.jme3.app.LostFocusBehavior;
import com.jme3.app.SimpleApplication;
[NOT=TAMARIN]
import com.jme3.app.VRAppState;
import com.jme3.app.VRConstants;
import com.jme3.app.VREnvironment;
[/NOT=TAMARIN]
import com.jme3.app.state.AppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.scene.shape.Box;
import com.jme3.scene.Geometry;
import com.jme3.math.Vector3f;
[IF=TAMARIN]
import com.onemillionworlds.tamarin.actions.ActionType;
import com.onemillionworlds.tamarin.actions.OpenXrActionState;
import com.onemillionworlds.tamarin.actions.actionprofile.Action;
import com.onemillionworlds.tamarin.actions.actionprofile.ActionManifest;
import com.onemillionworlds.tamarin.actions.actionprofile.ActionSet;
import com.onemillionworlds.tamarin.openxr.XrAppState;
import com.onemillionworlds.tamarin.openxr.XrSettings;
import com.onemillionworlds.tamarin.vrhands.HandSpec;
import com.onemillionworlds.tamarin.vrhands.VRHandsAppState;
import com.onemillionworlds.tamarin.vrhands.VRHandsAppState;
[/IF=TAMARIN]

import java.io.File;

public class [GAME_NAME] extends SimpleApplication{

    public static void main(String[] args) {
        [NOT=TAMARIN]
        AppSettings settings = new AppSettings(true);
        settings.put(VRConstants.SETTING_VRAPI, VRConstants.SETTING_VRAPI_OPENVR_LWJGL_VALUE);
        VREnvironment env = new VREnvironment(settings);
        env.initialize();
        if (env.isInitialized()){
            VRAppState vrAppState = new VRAppState(settings, env);

            [GAME_NAME] app = new [GAME_NAME](vrAppState);
            app.setLostFocusBehavior(LostFocusBehavior.Disabled);
            app.setSettings(settings);
            app.setShowSettings(false);
            app.start();
        }
        [/NOT=TAMARIN]
        [IF=TAMARIN]
            AppSettings settings = new AppSettings(true);
            settings.put("Renderer", AppSettings.LWJGL_OPENGL45); // OpenXR only supports relatively modern OpenGL
            settings.setTitle("[GAME_NAME_FULL]");
            settings.setVSync(false); // don't want to VSync to the monitor refresh rate, we want to VSync to the headset refresh rate (which tamarin implictly handles)

            [GAME_NAME] app = new [GAME_NAME]();
            app.setLostFocusBehavior(LostFocusBehavior.Disabled);
            app.setSettings(settings);
            app.setShowSettings(false);
            app.start();
        [/IF=TAMARIN]
    }

    public [GAME_NAME](AppState... appStates) {
        super(appStates);
    }

    @Override
    public void simpleInitApp(){
        [IF=TAMARIN]
        XrAppState xrState = new XrAppState();
        xrState.movePlayersFeetToPosition(new Vector3f(0,0,10));
        xrState.playerLookAtPosition(new Vector3f(0,0,0));

        getStateManager().attach(xrState);
        getStateManager().attach(new OpenXrActionState(manifest(), ActionSets.MAIN));
        getStateManager().attach(new VRHandsAppState(handSpec()));
        [/IF=TAMARIN]

        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);
    }

[IF=TAMARIN]

    private static ActionManifest manifest(){

        //see https://github.com/oneMillionWorlds/Tamarin/wiki/Action-Based-Vr for more details on actions

        Action handPose = Action.builder()
            .actionHandle(ActionHandles.HAND_POSE)
            .translatedName("Hand Pose")
            .actionType(ActionType.POSE)
            .withSuggestAllKnownAimPoseBindings()
            .build();

        return ActionManifest.builder()
            .withActionSet(ActionSet
                .builder()
                .name("main")
                .translatedName("Main Actions")
                .priority(1)
                .withAction(handPose)
                //add more actions here
                .build()
            ).build();
        }

    /**
     * The hand spec describes the openXR actions that are bound to the hand graphics
     * The hand model could also be changed here but the tamarin default is being used here
     */
    private static HandSpec handSpec(){
        return HandSpec.builder(
                ActionHandles.HAND_POSE,
                ActionHandles.HAND_POSE
            ).build();
    }

[/IF=TAMARIN]
}
