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

import com.ardor3d.math.Vector3;

/**
 *
 * @author Andreas Hauffe
 */
public class ArrowData {
    
    public final static int POSREF_TIP  = 0;
    public final static int POSREF_TAIL = 1;
    
    private final float[] position;
    private final float[] direction;
    private final float size;
    private final int pos_ref;
    
    private final int numberOfCones;

    public ArrowData(float[] position, float[] direction, float size, int pos_ref, int numberOfCones) {
        this.position = position;
        this.direction = direction;
        this.size = size;
        this.pos_ref = pos_ref;
        this.numberOfCones = numberOfCones;
    }

    public float[] getPosition() {
        return position;
    }

    public Vector3 getPositionAsVector3() {
        return new Vector3(position[0], position[1], position[2]);
    }

    public float[] getDirection() {
        return direction;
    }

    public Vector3 getDirectionAsVector3() {
        return new Vector3(direction[0], direction[1], direction[2]);
    }

    public float getSize() {
        return size;
    }

    public int getPos_ref() {
        return pos_ref;
    }

    public int getNumberOfCones() {
        return numberOfCones;
    }
}
