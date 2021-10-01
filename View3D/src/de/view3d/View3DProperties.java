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

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.openide.util.NbPreferences;

/**
 *
 * @author Andreas Hauffe
 */
public class View3DProperties {
    
    public static final int BACKGROUND_PLAIN = 0;
    public static final int BACKGROUND_GRADIENT = 1;
    
    private double netQuality;
    public static final String PROP_NETQUALITY = "netQuality";
    
    private Color color1;
    public static final String PROP_COLOR1 = "color1";

    private Color color2;
    public static final String PROP_COLOR2 = "color2";
    
    private final static View3DProperties instance = new View3DProperties();

    
    private View3DProperties(){
        init();
    }
    
    private void init(){
        netQuality = NbPreferences.forModule(View3DProperties.class).getDouble(PROP_NETQUALITY, 1.0);
        color1 = new Color(NbPreferences.forModule(View3DProperties.class).getInt(PROP_COLOR1, (new Color(255, 255, 255)).getRGB()));
        color2 = new Color(NbPreferences.forModule(View3DProperties.class).getInt(PROP_COLOR2, (new Color(204, 231, 255)).getRGB()));
    }
    
    public static View3DProperties getDefault(){
        return instance;
    }

    /**
     * Get the value of netQuality
     *
     * @return the value of netQuality
     */
    public double getNetQuality() {
        return netQuality;
    }

    /**
     * Set the value of netQuality
     *
     * @param netQuality new value of netQuality
     */
    public void setNetQuality(double netQuality) {
        double oldNetQuality = this.netQuality;
        this.netQuality = netQuality;
        NbPreferences.forModule(View3DProperties.class).putDouble(PROP_NETQUALITY, netQuality);
        propertyChangeSupport.firePropertyChange(PROP_NETQUALITY, oldNetQuality, netQuality);
    }

    /**
     * Get the value of color1
     *
     * @return the value of color1
     */
    public Color getColor1() {
        return color1;
    }

    /**
     * Set the value of color1
     *
     * @param color1 new value of color1
     */
    public void setColor1(Color color1) {
        Color oldColor1 = this.color1;
        this.color1 = color1;
        NbPreferences.forModule(View3DProperties.class).putInt(PROP_COLOR1, color1.getRGB());
        propertyChangeSupport.firePropertyChange(PROP_COLOR1, oldColor1, color1);
    }

    /**
     * Get the value of color2
     *
     * @return the value of color2
     */
    public Color getColor2() {
        return color2;
    }

    /**
     * Set the value of color2
     *
     * @param color2 new value of color2
     */
    public void setColor2(Color color2) {
        Color oldColor2 = this.color2;
        this.color2 = color2;
        NbPreferences.forModule(View3DProperties.class).putInt(PROP_COLOR2, color2.getRGB());
        propertyChangeSupport.firePropertyChange(PROP_COLOR2, oldColor2, color2);
    }
    
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
}
