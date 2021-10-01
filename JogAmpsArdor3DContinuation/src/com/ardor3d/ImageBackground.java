/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ardor3d;

import com.ardor3d.image.Texture;
import com.ardor3d.image.util.awt.AWTImageLoader;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.util.TextureManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 *
 * @author Andreas Hauffe
 */
public class ImageBackground extends Background{
    
    private Image image;

    public ImageBackground(Component canvas) {
        super(canvas);
    }
    
    public ImageBackground(Component canvas, BufferedImage image) {
        this(canvas);
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        update();
    }
    
    @Override
    protected void update(){
        
        int width = Math.max(canvas.getWidth(), 10);
        int height = Math.max(canvas.getHeight(), 10);
        backgroundQuad.resize(width, height);
        backgroundQuad.setTranslation(width/2.0, height/2.0, 0.0);

        BufferedImage gradientImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) gradientImage.getGraphics();

        g2d.setPaint(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        if (image != null){
            double scale = Math.min(width/(double)image.getWidth(null), height/(double)image.getHeight(null));
            double transX = width/2.0-(double)image.getWidth(null)*scale/2.0;
            double transY = height/2.0-(double)image.getHeight(null)*scale/2.0;
            g2d.drawImage(image, new AffineTransform(scale,0f,0f,scale,transX,transY), null);
        }

        g2d.dispose();

        final TextureState ts = new TextureState();
        ts.setTexture(TextureManager.loadFromImage(AWTImageLoader.makeArdor3dImage(gradientImage, false), Texture.MinificationFilter.Trilinear));

        backgroundQuad.setRenderState(ts);
    }
    
}
