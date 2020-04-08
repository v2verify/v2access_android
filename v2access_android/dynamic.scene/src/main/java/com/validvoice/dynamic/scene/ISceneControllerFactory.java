package com.validvoice.dynamic.scene;

public interface ISceneControllerFactory {

    SceneController CreateSceneController(SceneDirector sceneDirector, SceneLayout sceneLayout);

}
