/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ardor3d;

import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Quad;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 *
 * @author Andreas Hauffe
 */
public abstract class Background extends Node{

    protected final Quad backgroundQuad;
    
    protected final Component canvas;

    @SuppressWarnings("this-escape")
    public Background(Component canvas) {
        super("Background");
        
        this.canvas = canvas;
        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                update();
            }

        });
        
        int width = Math.max(canvas.getWidth(), 10);
        int height = Math.max(canvas.getHeight(), 10);
        
        backgroundQuad = new Quad("BackgroundQuad", width, height);
        backgroundQuad.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        backgroundQuad.getSceneHints().setOrthoOrder(1);

        attachChild(backgroundQuad);

        final ZBufferState zstate = new ZBufferState();
        zstate.setWritable(false);
        zstate.setEnabled(false);
        setRenderState(zstate);

        getSceneHints().setRenderBucketType(RenderBucketType.Skip);
    }
    
    public boolean renderUnto(Renderer renderer) {
        renderer.setOrtho();
        renderer.draw(this);
        renderer.unsetOrtho();
        return true;
    }
    
    protected abstract void update();
    
}
