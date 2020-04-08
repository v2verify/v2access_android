package com.validvoice.dynamic.scene;

public interface ISceneActionListener {

    void onActorOpening(int actorId);
    void onActorOpened(int actorId);
    void onActorClosing(int actorId);
    void onActorClosed(int actorId);
    void onActorShowing(int actorId);
    void onActorVisible(int actorId);
    void onActorHiding(int actorId);
    void onActorGone(int actorId);

}
