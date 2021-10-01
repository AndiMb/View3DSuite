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
package de.view3d.utilities;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Point;
import com.ardor3d.scenegraph.shape.Cone;
import com.ardor3d.util.geom.BufferUtils;
import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.nio.FloatBuffer;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Andreas Hauffe
 */
public class CoordinateSystem extends Node {

    private final AttributedString captionX;
    private final AttributedString captionY;
    private final AttributedString captionZ;

    private final int pointSize = 5;
    private final float distCaption = 0.02f;

    private final float arrowHeight = 0.025f;
    private final float arrowRadius = 0.0075f;

    private final int fontSize = 30;

    private final double margin = 0.1;

    private final CoordinateBounds coordinateBounds = new CoordinateBounds();

    private final ArrayList<Vector3> points = new ArrayList<>();

    /**
     * Creates a new instance of SceneGraph. The new instance will have the
     * capability to be attached and removed from a BranchGroup, but will
     * contain no data.
     *
     * @param name
     */
    public CoordinateSystem(AttributedString xLabel, AttributedString yLabel, AttributedString zLabel) {
        Font f = new Font(Font.SERIF, Font.PLAIN, fontSize);
        this.captionX = xLabel;
        captionX.addAttribute(TextAttribute.SIZE, f.getSize());
        captionX.addAttribute(TextAttribute.FAMILY, f.getStyle());
        this.captionY = yLabel;
        captionY.addAttribute(TextAttribute.SIZE, f.getSize());
        captionY.addAttribute(TextAttribute.FAMILY, f.getStyle());
        this.captionZ = zLabel;
        captionZ.addAttribute(TextAttribute.SIZE, f.getSize());
        captionZ.addAttribute(TextAttribute.FAMILY, f.getStyle());
    }

    public CoordinateBounds getCoordinateBounds() {
        return coordinateBounds;
    }

    public void update() {
        this.detachAllChildren();

        attachChild(createCoordinateSystem());

        /*Point3d upper = new Point3d();
         Point3d lower = new Point3d();
         upper.set(coordinateBounds.xmax+margin, coordinateBounds.ymax+margin, coordinateBounds.zmax+margin);
         lower.set(coordinateBounds.xmin-margin, coordinateBounds.ymin-margin, coordinateBounds.zmin-margin);

         setBounds(new BoundingBox(lower, upper));*/
    }

    private Node createCoordinateSystem() {

        Node lineGroup = new Node();

        Vector3[] normals = new Vector3[2];
        normals[0] = new Vector3(0.0, 0.0, 0.0);
        normals[1] = new Vector3(0.0, 0.0, 0.0);

        // Plain line x-axis
        Vector3[] plaPts = new Vector3[2];
        plaPts[0] = new Vector3(coordinateBounds.xmin - margin, 0.0, 0.0);
        plaPts[1] = new Vector3(coordinateBounds.xmax + margin, 0.0, 0.0);
        Line line = new Line("x-axis", plaPts, normals, null, null);
        line.setSolidColor(ColorRGBA.BLACK);
        lineGroup.attachChild(line);

        // x-axis Arrow
        Cone xarrow = new Cone("x-axis cone", 2, 15, arrowRadius, arrowHeight);
        Transform xtrans3D = new Transform();
        Matrix3 xrotmat = new Matrix3();
        xrotmat.fromAngles(0, -Math.PI / 2.0, 0);
        xtrans3D.setRotation(xrotmat);
        xtrans3D.setTranslation(new Vector3(coordinateBounds.xmax + margin, 0.0, 0.0));
        xarrow.setTransform(xtrans3D);
        xarrow.setSolidColor(ColorRGBA.BLACK);
        lineGroup.attachChild(xarrow);

        // Plain line y-axis
        plaPts = new Vector3[2];
        plaPts[0] = new Vector3(0.0, coordinateBounds.ymin - margin, 0.0);
        plaPts[1] = new Vector3(0.0, coordinateBounds.ymax + margin, 0.0);
        line = new Line("y-axis", plaPts, normals, null, null);
        line.setSolidColor(ColorRGBA.BLACK);
        lineGroup.attachChild(line);

        // y-axis Arrow
        Cone yarrow = new Cone("y-axis cone", 2, 15, arrowRadius, arrowHeight);
        Transform ytrans3D = new Transform();
        Matrix3 yrotmat = new Matrix3();
        yrotmat.fromAngles(Math.PI / 2.0, 0, 0);
        ytrans3D.setRotation(yrotmat);
        ytrans3D.setTranslation(new Vector3(0.0, coordinateBounds.ymax + margin, 0.0));
        yarrow.setTransform(ytrans3D);
        yarrow.setSolidColor(ColorRGBA.BLACK);
        lineGroup.attachChild(yarrow);

        // Plain line z-axis
        plaPts = new Vector3[2];
        plaPts[0] = new Vector3(0.0, 0.0, coordinateBounds.zmin - margin);
        plaPts[1] = new Vector3(0.0, 0.0, coordinateBounds.zmax + margin);
        line = new Line("z-axis", plaPts, normals, null, null);
        line.setSolidColor(ColorRGBA.BLACK);
        lineGroup.attachChild(line);

        // z-axis Arrow
        Cone zarrow = new Cone("z-axis cone", 2, 15, arrowRadius, arrowHeight);
        Transform ztrans3D = new Transform();
        Matrix3 zrotmat = new Matrix3();
        zrotmat.fromAngles(Math.PI, 0, 0);
        ztrans3D.setRotation(zrotmat);
        ztrans3D.setTranslation(new Vector3(0.0, 0.0, coordinateBounds.zmax + margin));
        zarrow.setTransform(ztrans3D);
        zarrow.setSolidColor(ColorRGBA.BLACK);
        lineGroup.attachChild(zarrow);

        for (Vector3 p : points) {
            lineGroup.attachChild(createPointAt(p, pointSize));
        }
        lineGroup.attachChild(new com.ardor3d.RasterTextLabel(captionX, Color.BLACK, coordinateBounds.xmax + margin, distCaption, distCaption));
        lineGroup.attachChild(new com.ardor3d.RasterTextLabel(captionY, Color.BLACK, distCaption, coordinateBounds.ymax + margin, distCaption));
        lineGroup.attachChild(new com.ardor3d.RasterTextLabel(captionZ, Color.BLACK, distCaption, distCaption, coordinateBounds.zmax + margin));

        // Add a material state
        final MaterialState ms = new MaterialState();
        // Pull diffuse color for front from mesh color
        ms.setColorMaterial(MaterialState.ColorMaterial.AmbientAndDiffuse);
        ms.setColorMaterialFace(MaterialState.MaterialFace.FrontAndBack);
        lineGroup.setRenderState(ms);

        return lineGroup;
    }

    public void addPoints(Collection<Vector3> points) {
        this.points.addAll(points);
    }

    public void clearPointCollection() {
        this.points.clear();
    }

    private Mesh createPointAt(Vector3 pos, int size) {
        
        int nNumPoints = 1;

        final FloatBuffer pointData = BufferUtils.createFloatBuffer(nNumPoints * 3);
        for (int i = 0; i < nNumPoints; i++) {
            pointData.put(pos.getXf()); // x 
            pointData.put(pos.getYf()); // y 
            pointData.put(pos.getZf()); // z 
        }
        
        Vector3[] normals = new Vector3[]{new Vector3(0.0f, 0.0f, 0.0f)};

        final Point pointsA = new Point("points", pointData, BufferUtils.createFloatBuffer(normals), null, null);

        pointsA.setDefaultColor(ColorRGBA.BLACK);
        pointsA.setSolidColor(ColorRGBA.BLACK);
        pointsA.setAntialiased(true);
        pointsA.setPointSize(size);

        final BlendState bState = new BlendState();
        bState.setBlendEnabled(true);
        pointsA.setRenderState(bState);

        // Add a material state
        final MaterialState ms = new MaterialState();
        // Pull diffuse color for front from mesh color
        ms.setColorMaterial(MaterialState.ColorMaterial.AmbientAndDiffuse);
        ms.setColorMaterialFace(MaterialState.MaterialFace.FrontAndBack);
        pointsA.setRenderState(ms);
        
        return pointsA;
    }

    public static class CoordinateBounds {

        private double xmax = 1.0;
        private double xmin = -1.0;
        private double ymax = 1.0;
        private double ymin = -1.0;
        private double zmax = 1.0;
        private double zmin = -1.0;

        public CoordinateBounds() {
        }

        private void checkBounds() {
            if (xmax - xmin == 0.0
                    && ymax - ymin == 0.0
                    && zmax - zmin == 0.0) {
                reset();
            }
        }

        public void reset() {
            xmax = 1.0;
            xmin = -1.0;
            ymax = 1.0;
            ymin = -1.0;
            zmax = 1.0;
            zmin = -1.0;
        }

        public void setValues(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            xmin = minX;
            ymin = minY;
            zmin = minZ;
            xmax = maxX;
            ymax = maxY;
            zmax = maxZ;
            checkBounds();
        }

        public double getXmax() {
            return xmax;
        }

        public void setXmax(double xmax) {
            this.xmax = xmax;
            checkBounds();
        }

        public double getXmin() {
            return xmin;
        }

        public void setXmin(double xmin) {
            this.xmin = xmin;
            checkBounds();
        }

        public double getYmax() {
            return ymax;
        }

        public void setYmax(double ymax) {
            this.ymax = ymax;
            checkBounds();
        }

        public double getYmin() {
            return ymin;
        }

        public void setYmin(double ymin) {
            this.ymin = ymin;
            checkBounds();
        }

        public double getZmax() {
            return zmax;
        }

        public void setZmax(double zmax) {
            this.zmax = zmax;
            checkBounds();
        }

        public double getZmin() {
            return zmin;
        }

        public void setZmin(double zmin) {
            this.zmin = zmin;
            checkBounds();
        }
    }
}
