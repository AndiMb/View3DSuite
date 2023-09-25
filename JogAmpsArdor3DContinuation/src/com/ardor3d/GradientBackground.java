/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ardor3d;

import com.ardor3d.framework.jogl.awt.JoglSwingCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.awt.AWTImageLoader;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.util.TextureManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Andreas Hauffe
 */
public class GradientBackground extends Background{

    private Color colorTop = Color.WHITE;
    private Color colorButtom = Color.BLUE;
    
    public GradientBackground(Component canvas) {
        super(canvas);
    }
    
    public GradientBackground(Component canvas, Color colorTop, Color colorButtom) {
        this(canvas);
        this.colorTop = colorTop;
        this.colorButtom = colorButtom;
    }

    public Color getColorTop() {
        return colorTop;
    }

    public void setColorTop(Color colorTop) {
        this.colorTop = colorTop;
        update();
    }

    public Color getColorButtom() {
        return colorButtom;
    }

    public void setColorButtom(Color colorButtom) {
        this.colorButtom = colorButtom;
        update();
    }
    
    @Override
    protected void update(){
        
        float[] pixelScale = new float[]{1.0f, 1.0f};
        if (canvas instanceof JoglSwingCanvas joglSwingCanvas){
            pixelScale = joglSwingCanvas.getCurrentSurfaceScale(new float[2]);
        }
        int width = (int)(Math.max(canvas.getWidth(), 10) * pixelScale[0]);
        int height = (int)(Math.max(canvas.getHeight(), 10) * pixelScale[0]);
        backgroundQuad.resize(width, height);
        backgroundQuad.setTranslation(width/2.0, height/2.0, 0.0);

        BufferedImage gradientImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) gradientImage.getGraphics();

        GradientPaint gp = new GradientPaint(0, 0, colorButtom, 0, height, colorTop);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);

        g2d.dispose();

        final TextureState ts = new TextureState();
        ts.setTexture(TextureManager.loadFromImage(AWTImageLoader.makeArdor3dImage(gradientImage, false), Texture.MinificationFilter.Trilinear));

        backgroundQuad.setRenderState(ts);
    }
    
}
