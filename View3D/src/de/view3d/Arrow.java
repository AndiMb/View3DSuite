/*
 *  This program developed in Java is based on the netbeans platform and is used
 *  to design and to analyse composite structures by means of analytical and 
 *  numerical methods.
 * 
 *  Further information can be found here:
 *  http://www.elamx.de
 *    
 *  Copyright (C) 2021 Technische Universität Dresden - Andreas Hauffe
 * 
 *  This file is part of eLamX².
 *
 *  eLamX² is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  eLamX² is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with eLamX².  If not, see <http://www.gnu.org/licenses/>.
 */
package de.view3d;

import com.ardor3d.ArrowPrimitive;
import com.ardor3d.DoubleArrowPrimitive;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.TransparencyType;

/**
 *
 * @author Andreas Hauffe
 */
public class Arrow extends Node {
    
    private final static float EPS = 0.0000001f;
    
    private final static float coneLength = 0.03f;
    private final static float coneRadius = 0.012f;
    
    private final static float zylinderLength = 0.045f;
    private final static float zylinderRadius = 0.0065f;
    
    public Arrow(ArrowData data){
        this(data, new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f));
    }

    public Arrow(ArrowData data, ColorRGBA color) {
        float size = data.getSize();
        int numberOfCones = data.getNumberOfCones();
        
        ColorRGBA c = new ColorRGBA(color);
        c.setAlpha(0.8f);
        
        Transform arrowTrans = new Transform();
        
        Vector3 yDir = data.getDirectionAsVector3();
        yDir.normalizeLocal();
        Vector3 temp = new Vector3(0.0f, 0.0f, 1.0f);
        if (1.0f - Math.abs(temp.dot(yDir)) < EPS){
            temp = new Vector3(1.0f, 0.0f, 0.0f);
        }
        Vector3 xDir = new Vector3();
        yDir.cross(temp, xDir);
        xDir.normalizeLocal();
        Vector3 zDir = new Vector3();
        xDir.cross(yDir, zDir);
        
        Matrix3 transMat = new Matrix3();
        transMat.setColumn(0, xDir);
        transMat.setColumn(1, yDir);
        transMat.setColumn(2, zDir);
        
        arrowTrans.setRotation(transMat);
        arrowTrans.setTranslation(data.getPositionAsVector3());
        
        Node child;
        if (numberOfCones == 1){
            ArrowPrimitive arrow = new ArrowPrimitive("Arrow", size*coneRadius, size*coneLength, size*zylinderRadius, size*zylinderLength);
            arrow.setSolidColor(c);
            child = arrow;
        }else{
            DoubleArrowPrimitive arrow = new DoubleArrowPrimitive("DoubleArrow", size*coneRadius, size*coneLength, size*zylinderRadius, size*zylinderLength);
            arrow.setSolidColor(c);
            child = arrow;
        }
        
        // Add a material state
        final MaterialState ms = new MaterialState();
        // Pull diffuse color for front from mesh color
        ms.setColorMaterial(MaterialState.ColorMaterial.Diffuse);
        ms.setColorMaterialFace(MaterialState.MaterialFace.Front);
        // Set shininess for front and back
        ms.setShininess(MaterialState.MaterialFace.Front, 100);
        child.setRenderState(ms);

        BlendState blend = new BlendState();
        blend.setBlendEnabled(true);
        child.setRenderState(blend);

        child.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
        child.getSceneHints().setTransparencyType(TransparencyType.TwoPass);
        
        
        if (data.getPos_ref() == ArrowData.POSREF_TAIL){
            Transform cylTrans = new Transform();
            cylTrans.setTranslation(new Vector3(0.0f, size*(zylinderLength + coneLength * (numberOfCones == 1 ? 1 : 2)) , 0.0f));
            
            Node cylTG = new Node();
            cylTG.setTransform(cylTrans);
            cylTG.attachChild(child);
            
            child = cylTG;
        }
        
        setTransform(arrowTrans);
        attachChild(child);
    }
    
    public static float getLength(){
        return coneLength + zylinderLength;
    }
}
