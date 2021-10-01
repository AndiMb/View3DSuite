/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ardor3d;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Cone;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import java.io.IOException;

/**
 * <code>Arrow</code> is basically a cylinder with a pyramid on top.
 */
public class ArrowPrimitive extends Node {

    protected double cone_radius, cone_height;
    protected double cylinder_radius, cylinder_height;

    protected static final Quaternion rotator = new Quaternion().applyRotationX(MathUtils.HALF_PI);

    public ArrowPrimitive() {}

    public ArrowPrimitive(final String name) {
        super(name);
    }

    public ArrowPrimitive(final String name, double cone_radius, double cone_height, double cylinder_radius, double cylinder_height) {
        super(name);
        this.cone_radius = cone_radius;
        this.cone_height = cone_height;
        this.cylinder_radius = cylinder_radius;
        this.cylinder_height = cylinder_height;

        buildArrow();
    }

    public void buildArrow() {
        // Start with cylinders:
        final Cylinder base = new Cylinder("base", 4, 16, cylinder_radius, cylinder_height, true);
        base.getMeshData().rotatePoints(rotator);
        base.getMeshData().rotateNormals(rotator);
        base.getMeshData().translatePoints(0, -cylinder_height/2.0-cone_height, 0);
        attachChild(base);
        base.updateModelBound();
        final Cone tip = new Cone("tip", 4, 16, (float)cone_radius, (float)cone_height);
        tip.getMeshData().rotatePoints(rotator);
        tip.getMeshData().rotateNormals(rotator);
        tip.getMeshData().translatePoints(0, -cone_height/2.0, 0);
        attachChild(tip);
        tip.updateModelBound();
    }

    public void setSolidColor(final ReadOnlyColorRGBA color) {
        for (int x = 0; x < getNumberOfChildren(); x++) {
            if (getChild(x) instanceof Mesh) {
                ((Mesh) getChild(x)).setSolidColor(color);
            }
        }
    }

    public void setDefaultColor(final ReadOnlyColorRGBA color) {
        for (int x = 0; x < getNumberOfChildren(); x++) {
            if (getChild(x) instanceof Mesh) {
                ((Mesh) getChild(x)).setDefaultColor(color);
            }
        }
    }

    @Override
    public void write(final OutputCapsule capsule) throws IOException {
        super.write(capsule);
        capsule.write(cone_radius, "cone_radius", 1);
        capsule.write(cone_height, "cone_height", .25);
        capsule.write(cylinder_radius, "cylinder_radius", 1);
        capsule.write(cone_height, "cone_height", .25);
    }

    @Override
    public void read(final InputCapsule capsule) throws IOException {
        super.read(capsule);
        cone_radius = capsule.readDouble("cone_radius", 1);
        cone_height = capsule.readDouble("cone_height", .25);
        cylinder_radius = capsule.readDouble("cylinder_radius", 1);
        cylinder_height = capsule.readDouble("cylinder_height", .25);
    }
}
