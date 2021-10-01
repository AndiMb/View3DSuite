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

import com.ardor3d.Background;
import com.ardor3d.GradientBackground;
import com.ardor3d.ImageBackground;
import com.ardor3d.MouseControl;
import com.ardor3d.ScreenShotImageExporter2;
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameHandler;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.Updater;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.framework.jogl.awt.JoglSwingCanvas;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.input.logical.DummyControllerWrapper;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Point;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.TransparencyType;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.Timer;
import com.ardor3d.util.screen.ScreenExporter;
import de.view3d.utilities.CoordinateSystem;
import de.view3d.utilities.MultiBorderLayout;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Andreas Hauffe
 */
public class View3D extends JPanel implements Scene, Updater, Runnable {

    private JoglSwingCanvas canvas;
    private AwtMouseManager mouseManager;
    private PhysicalLayer pl;
    private LogicalLayer logicalLayer;
    private Timer timer;
    private FrameHandler frameWork;

    //private final JoglAwtCanvas canvas;
    private volatile boolean exit = false;
    private LightState lightState;
    private MouseControl control;
    private final Node root;
    private final Node objRoot;
    private final Node objTrans;
    private final Node geometryRoot;
    private final Node addGeometryRoot;
    private final Node wireframeRoot;

    private Background background;

    private boolean wireframeAllowed = true;
    private boolean showWireframe = true;

    private final WireframeState ws = new WireframeState();

    private BoundingBox bounds;

    private CoordinateSystem coordSys;

    private boolean backgroundImageAllowed = false;
    private BufferedImage hud = null;
    private boolean showHud = false;
    private final int paddingLegendTop = 10;
    private final int paddingLegendLeft = 10;
    private Vector3 origScaleVec;
    private Vector3 actScaleVec = new Vector3(1.0, 1.0, 1.0);
    
    Thread myThread;

    private boolean firstInit = true;

    private static int threadInitNumber;

    private boolean inheritNetTransparency = false;

    private volatile boolean update = true;

    public View3D() {
        this(new AttributedString("x"), new AttributedString("y"), new AttributedString("z"), false);
    }

    public View3D(AttributedString xLabel, AttributedString yLabel, AttributedString zLabel, boolean allowSwitchNet) {
        this(xLabel, yLabel, zLabel, allowSwitchNet, false);
    }

    public View3D(AttributedString xLabel, AttributedString yLabel, AttributedString zLabel, boolean allowSwitchNet, boolean backgroundImageAllowed) {
        System.setProperty("ardor3d.useMultipleContexts", "true");
        System.setProperty("jogl.gljpanel.noglsl", "true");

        this.wireframeAllowed = allowSwitchNet;
        this.backgroundImageAllowed = backgroundImageAllowed;

        setLayout(new MultiBorderLayout());
        //setLayout(new BorderLayout());

        this.root = new Node("rootNode");
        this.objRoot = new Node("ObjectRootNode");
        this.objTrans = new Node("ObjectTransformationNode");
        this.geometryRoot = new Node("GeometryRootNode");
        this.addGeometryRoot = new Node("additionalGeometryRootNode");
        this.wireframeRoot = new Node("WireframeRootNode");

        coordSys = new CoordinateSystem(xLabel, yLabel, zLabel);
        coordSys.update();

        add(getButtonBar(), BorderLayout.WEST);

        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {

                if (addNotify
                        && (((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0)
                        && View3D.this.isShowing()
                        || (((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0)
                        && View3D.this.isShowing()))) {
                    View3D.this.setSize(10, 10);
                    exit = false;
                    initComponents();
                    myThread = new Thread(View3D.this, "View3D+" + threadInitNumber++);
                    myThread.start();
                    addNotify = false;
                }

                if (removeNotify && ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0)) {
                    exit = true;
                    //myThread.stop();
                    clear();
                    System.gc();
                    removeNotify = false;
                }
            }
        });
    }

    public void setInheritNetTransparency(boolean inheritNetTransparency) {
        this.inheritNetTransparency = inheritNetTransparency;
    }

    public void addAdditionalButtonBar(JToolBar toolBar) {
        toolBar.setOrientation(JToolBar.VERTICAL);
        add(toolBar, BorderLayout.WEST);
    }

    public boolean isWireframeAllowed() {
        return wireframeAllowed;
    }

    public void setWireframeAllowed(boolean wireframeAllowed) {
        this.wireframeAllowed = wireframeAllowed;
    }

    public boolean isBackgroundImageAllowed() {
        return backgroundImageAllowed;
    }

    public void setBackgroundImageAllowed(boolean backgroundImageAllowed) {
        this.backgroundImageAllowed = backgroundImageAllowed;
    }

    public void setHUDImage(BufferedImage image) {
        hud = image;
    }

    public boolean isShowHud() {
        return showHud;
    }

    public void setShowHud(boolean showHud) {
        this.showHud = showHud;
        repaint();
    }

    private boolean removeNotify = false;
    private boolean addNotify = false;

    @Override
    public void removeNotify() {
        super.removeNotify(); //To change body of generated methods, choose Tools | Templates.
        removeNotify = true;
    }

    @Override
    public void addNotify() {
        super.addNotify(); //To change body of generated methods, choose Tools | Templates.
        addNotify = true;
    }

    private void initComponents() {

        final JoglCanvasRenderer canvasRenderer = new JoglCanvasRenderer(this);
        final DisplaySettings settings = new DisplaySettings(
                10, // the canvas (unrotated) width
                10, // the canvas (unrotated) height 
                24, // the number of color bits used to represent the color of a single pixel
                0, // the number of times per second to repaint the canvas
                0, // the numner of bits used to represent the translucency of a single pixel
                24, // the number of bits making up the z-buffer
                0, // the number of bits making up the stencil buffer
                1, // the number of samples used to anti-alias
                false, // true if the canvas should assume exclusive access to the screen
                false); // true if the canvas should be rendered stereoscopically (for 3D glasses)
        canvas = new JoglSwingCanvas(settings, canvasRenderer) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if (hud != null && showHud) {
                    g.drawImage(hud, paddingLegendLeft, paddingLegendTop, canvas);
                }
            }
        };
        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                resizeCanvas();
            }

        });
        background = new GradientBackground(canvas);
        ((GradientBackground) background).setColorButtom(View3DProperties.getDefault().getColor2());
        ((GradientBackground) background).setColorTop(View3DProperties.getDefault().getColor1());

        mouseManager = new AwtMouseManager(canvas);
        pl = new PhysicalLayer(new AwtKeyboardWrapper(canvas),
                new AwtMouseWrapper(canvas, mouseManager),
                DummyControllerWrapper.INSTANCE,
                new AwtFocusWrapper(canvas));

        logicalLayer = new LogicalLayer();
        logicalLayer.registerInput(canvas, pl);

        timer = new Timer();
        frameWork = new FrameHandler(timer);
        frameWork.addUpdater(this);
        frameWork.addCanvas(canvas);

        coordSys.update();

        add(canvas, BorderLayout.CENTER);
    }

    public void addPoints(Collection<Vector3> points) {
        coordSys.addPoints(points);
    }

    public void setPoints(Collection<Vector3> points) {
        coordSys.clearPointCollection();
        coordSys.addPoints(points);
    }

    private JToolBar getButtonBar() {
        JToolBar buttonBar = new JToolBar();

        // init the buttonbar
        buttonBar.setOrientation(JToolBar.VERTICAL);

        // margins for all buttons
        Insets margins = new Insets(0, 0, 0, 0);

        JButton xyButton = new JButton();
        xyButton.setMargin(margins);
        xyButton.setIcon(ImageUtilities.loadImageIcon("de/view3d/resources/x-y.png", false));
        xyButton.setToolTipText(NbBundle.getMessage(View3D.class, "xyButton.tip"));
        xyButton.addActionListener((ActionEvent e) -> {
            viewXYPlane();
        });

        JButton xzButton = new JButton();
        xzButton.setMargin(margins);
        xzButton.setIcon(ImageUtilities.loadImageIcon("de/view3d/resources/x-z.png", false));
        xzButton.setToolTipText(NbBundle.getMessage(View3D.class, "xzButton.tip"));
        xzButton.addActionListener((ActionEvent e) -> {
            viewXZPlane();
        });

        JButton yzButton = new JButton();
        yzButton.setMargin(margins);
        yzButton.setIcon(ImageUtilities.loadImageIcon("de/view3d/resources/y-z.png", false));
        yzButton.setToolTipText(NbBundle.getMessage(View3D.class, "yzButton.tip"));
        yzButton.addActionListener((ActionEvent e) -> {
            viewYZPlane();
        });

        JButton diagButton = new JButton();
        diagButton.setMargin(margins);
        diagButton.setIcon(ImageUtilities.loadImageIcon("de/view3d/resources/diag.png", false));
        diagButton.setToolTipText(NbBundle.getMessage(View3D.class, "diagButton.tip"));
        diagButton.addActionListener((ActionEvent e) -> {
            viewDiagonal();
        });
        JButton fitViewButton = new JButton();
        fitViewButton.setMargin(margins);
        fitViewButton.setIcon(ImageUtilities.loadImageIcon("de/view3d/resources/fit.png", false));
        fitViewButton.setToolTipText(NbBundle.getMessage(View3D.class, "fitViewButton.tip"));
        fitViewButton.addActionListener((ActionEvent e) -> {
            fitView();
        });

        buttonBar.add(xyButton);
        buttonBar.add(xzButton);
        buttonBar.add(yzButton);
        buttonBar.add(diagButton);
        buttonBar.add(fitViewButton);

        if (wireframeAllowed) {
            JToggleButton netButton = new JToggleButton();
            netButton.setMargin(margins);
            netButton.setIcon(ImageUtilities.loadImageIcon("de/view3d/resources/net.png", false));
            netButton.setToolTipText(NbBundle.getMessage(View3D.class, "netButton.tip"));
            netButton.addActionListener((ActionEvent e) -> {
                AbstractButton abstractButton = (AbstractButton) e.getSource();
                useNetForCriterion(abstractButton.getModel().isSelected());
            });
            netButton.setSelected(showWireframe);
            //buttonBar.addSeparator();
            buttonBar.add(netButton);
        } else {
            showWireframe = false;
        }

        if (backgroundImageAllowed) {
            JButton imageButton = new JButton();
            imageButton.setMargin(margins);
            imageButton.setIcon(ImageUtilities.loadImageIcon("de/view3d/resources/fileopen24.png", false));
            imageButton.setToolTipText(NbBundle.getMessage(View3D.class, "backgroundimagebutton.tip"));
            imageButton.addActionListener((ActionEvent e) -> {
                File basePath = new File(System.getProperty("user.home"));
                File file = new FileChooserBuilder("database-dir").setTitle(NbBundle.getMessage(View3D.class, "OpenFileDialog.Title")).setDefaultWorkingDirectory(basePath).setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        String name = f.getName().toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".JPG") || (f.isDirectory() && !f.isHidden());
                    }
                    
                    @Override
                    public String getDescription() {
                        return NbBundle.getMessage(View3D.class, "OpenFileDialog.Description");
                    }
                }).setSelectionApprover((File[] selection) -> {
                    if (selection.length > 1) {
                        return false;
                    }
                    return selection[0].getName().endsWith(".jpg") | selection[0].getName().endsWith(".JPG");
                }).setApproveText(NbBundle.getMessage(View3D.class, "OpenFileDialog.ApproveText")).setFileHiding(true).showOpenDialog();
                setBackgroundImage(file);
            });
            buttonBar.add(imageButton);
        }

        return buttonBar;
    }

    @Override
    public boolean renderUnto(Renderer renderer) {
        ContextGarbageCollector.doRuntimeCleanup(renderer);

        background.renderUnto(renderer);

        objRoot.draw(renderer);

        if (screenExportable != null) {
            // force any waiting scene elements to be rendered.
            renderer.renderBuckets();
            ScreenExporter.exportCurrentScreen(renderer, screenExportable);
            screenExportable = null;
        }
        return true;
    }

    public void setZScale(double scale) {
        actScaleVec = new Vector3(1.0, 1.0, scale);
        scale();
    }

    private void scale() {
        if (objRoot != null) {
            update = false;
            objTrans.removeFromParent();

            Transform tempTrans = new Transform();
            tempTrans.setScale(actScaleVec);
            BoundingBox bd = (BoundingBox) bounds.transform(tempTrans, null);

            Vector3 temp = new Vector3();
            temp.setX(origScaleVec.getX() * actScaleVec.getX());
            temp.setY(origScaleVec.getY() * actScaleVec.getY());
            temp.setZ(origScaleVec.getZ() * actScaleVec.getZ());
            tempTrans = new Transform();
            tempTrans.setScale(temp);

            geometryRoot.setTransform(tempTrans);
            if (wireframeAllowed) {
                temp = new Vector3();
                temp.setX(origScaleVec.getX() * actScaleVec.getX());
                temp.setY(origScaleVec.getY() * actScaleVec.getY());
                temp.setZ(origScaleVec.getZ() * actScaleVec.getZ());
                tempTrans = new Transform();
                tempTrans.setScale(temp);
                wireframeRoot.setTransform(tempTrans);
            }

            //update CoordinateSystem
            coordSys.removeFromParent();
            coordSys.getCoordinateBounds().setValues(
                    bd.getCenter().getX() - bd.getXExtent(),
                    bd.getCenter().getY() - bd.getYExtent(),
                    bd.getCenter().getZ() - bd.getZExtent(),
                    bd.getCenter().getX() + bd.getXExtent(),
                    bd.getCenter().getY() + bd.getYExtent(),
                    bd.getCenter().getZ() + bd.getZExtent());

            coordSys.update();
            objTrans.attachChild(coordSys);
            objRoot.attachChild(objTrans);
            objRoot.updateGeometricState(0);

            update = true;
        }

    }

    private ScreenShotImageExporter2 screenExportable = null;

    public void saveScreenshot(File file, String fileFormat) {
        // only works after the 2nd frame
        frameWork.updateFrame();
        screenExportable = new ScreenShotImageExporter2(file, fileFormat, true);
        frameWork.updateFrame();
    }

    @Override
    public void init() {
        /**
         * Create a ZBuffer to display pixels closest to the camera above
         * farther ones.
         */
        ZBufferState buf = new ZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        objRoot.setRenderState(buf);

        // ---- LIGHTS
        DirectionalLight headlight = new DirectionalLight();
        headlight.setDirection(0.1f, 0.1f, -1.0f);
        headlight.setEnabled(true);

        /**
         * Attach the light to a lightState and the lightState to rootNode.
         */
        lightState = new LightState();
        lightState.setEnabled(true);
        lightState.attach(headlight);
        objRoot.setRenderState(lightState);

        objRoot.getSceneHints().setRenderBucketType(RenderBucketType.Opaque);

        control = new MouseControl(objTrans);
        control.setupMouseTriggers(logicalLayer);

        objTrans.attachChild(coordSys);
        objTrans.attachChild(geometryRoot);
        
        
        /*OffsetState ofState = new OffsetState();
        ofState.setFactor(-1.0f);
        ofState.setUnits(1.0f);
        ofState.setTypeEnabled(OffsetState.OffsetType.Line, true);
        
        objTrans.setRenderState(ofState);*/
        
        objRoot.attachChild(objTrans);

        ws.setLineWidth(1.0f);
        ws.setAntialiased(true);
        wireframeRoot.setRenderState(ws);
        wireframeRoot.getSceneHints().setLightCombineMode(LightCombineMode.Off);

        root.attachChild(objRoot);
        root.attachChild(background);
        root.updateGeometricState(0);

        if (firstInit) {
            viewDiagonal();
            firstInit = false;
        }
    }

    @Override
    public void update(ReadOnlyTimer rot) {
        if (update) {
            logicalLayer.checkTriggers(rot.getTimePerFrame());
            root.updateGeometricState(rot.getTimePerFrame(), true);
        }
    }

    //private static void resizeCanvas(JoglAwtCanvas canvas) {
    private void resizeCanvas() {
        int w = canvas.getSurfaceWidth();
        int h = canvas.getSurfaceHeight();
        double r = (double) w / (double) h;

        Camera cam = canvas.getCanvasRenderer().getCamera();
        if (null != cam) {
            cam.setProjectionMode(ProjectionMode.Parallel);
            cam.resize(w, h);

            cam.setFrustumPerspective(cam.getFovY(), r, cam.getFrustumNear(),
                    cam.getFrustumFar());
        }
    }

    @Override
    public void run() {
        frameWork.init();
        long start;
        long end;
        long sleep;
        long waitTime = 30;
        while (!exit) {
            start = System.currentTimeMillis();
            if (update) {
                frameWork.updateFrame();
            }
            end = System.currentTimeMillis();
            if (end - start < waitTime) {
                sleep = waitTime - start + end;
            } else {
                sleep = 10;
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public PickResults doPick(Ray3 ray3) {
        return null;
    }

    public synchronized void setShape3D(List<Mesh> shapes, double scale) {
        update = false;

        objTrans.detachChild(geometryRoot);
        objTrans.detachChild(coordSys);
        geometryRoot.detachAllChildren();
        wireframeRoot.detachAllChildren();

        double xmin = 0.0;
        double xmax = 0.0;
        double ymin = 0.0;
        double ymax = 0.0;
        double zmin = 0.0;
        double zmax = 0.0;

        if (!shapes.isEmpty()) {
            xmin = Double.MAX_VALUE;
            xmax = -Double.MAX_VALUE;
            ymin = Double.MAX_VALUE;
            ymax = -Double.MAX_VALUE;
            zmin = Double.MAX_VALUE;
            zmax = -Double.MAX_VALUE;
            for (Mesh shape : shapes) {
                geometryRoot.attachChild(shape);

                if (wireframeAllowed && !(shape instanceof Line) && !(shape instanceof Point)) {
                    Mesh copy = new Mesh();
                    copy.setMeshData(shape.getMeshData());
                    ReadOnlyColorRGBA c = shape.getDefaultColor();
                    copy.setDefaultColor(ColorRGBA.BLACK);
                    if (inheritNetTransparency) {
                        setTransparent(copy, c.getAlpha());
                    }
                    wireframeRoot.attachChild(copy);

                    /*Mesh copy = new Mesh();
                    copy.setMeshData(shape.getMeshData().makeCopy());*/
 /*Mesh copy = shape.makeCopy(false);*/
 /*copy.setSolidColor(ColorRGBA.BLACK);
                    wireframeRoot.attachChild(copy);*/
                }

                shape.setModelBound(new BoundingBox());
                BoundingBox box = (BoundingBox) shape.getModelBound();
                ReadOnlyVector3 center = box.getCenter();
                double temp = box.getXExtent();
                xmax = Math.max(xmax, center.getX() + temp);
                xmin = Math.min(xmin, center.getX() - temp);

                temp = box.getYExtent();
                ymax = Math.max(ymax, center.getY() + temp);
                ymin = Math.min(ymin, center.getY() - temp);

                temp = box.getZExtent();
                zmax = Math.max(zmax, center.getZ() + temp);
                zmin = Math.min(zmin, center.getZ() - temp);
            }
        }
        origScaleVec = new Vector3(scale, scale, scale);
        geometryRoot.setScale(origScaleVec);
        addGeometryRoot.setScale(origScaleVec);
        wireframeRoot.setScale(origScaleVec);
        xmax *= scale;
        xmin *= scale;
        ymax *= scale;
        ymin *= scale;
        zmax *= scale;
        zmin *= scale;

        Vector3 center = new Vector3((xmax + xmin) / 2.0, (ymax + ymin) / 2.0, (zmax + zmin) / 2.0);
        bounds = new BoundingBox(center, (xmax - xmin) / 2.0, (ymax - ymin) / 2.0, (zmax - zmin) / 2.0);

        //update CoordinateSystem
        coordSys.getCoordinateBounds().setValues(xmin, ymin, zmin, xmax, ymax, zmax);
        coordSys.update();

        if (wireframeAllowed && showWireframe) {
            objTrans.attachChild(wireframeRoot);
        }
        objTrans.attachChild(geometryRoot);
        objTrans.attachChild(addGeometryRoot);
        objTrans.attachChild(coordSys);

        root.updateGeometricState(0);

        update = true;
    }

    private void setTransparent(Mesh mesh, float trans) {

        ReadOnlyColorRGBA c = mesh.getDefaultColor();
        mesh.setDefaultColor(new ColorRGBA(c.getRed(), c.getGreen(), c.getBlue(), trans));

        BlendState blend = new BlendState();
        blend.setBlendEnabled(true);
        mesh.setRenderState(blend);

        if (trans > 0.999) {
            mesh.getSceneHints().setRenderBucketType(RenderBucketType.Opaque);
        } else {
            mesh.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
            mesh.getSceneHints().setTransparencyType(TransparencyType.TwoPass);
        }
    }

    private void clear() {
        if (canvas != null) {
            remove(canvas);
            canvas.destroy();
        }
        canvas = null;
        mouseManager = null;
        pl = null;
        logicalLayer = null;
        frameWork = null;
        timer = null;
        lightState = null;
        control = null;
        //objRoot.detachAllChildren();
        //objTrans.detachAllChildren();
        //geometryRoot.detachAllChildren();
        //addGeometryRoot.detachAllChildren();
        //wireframeRoot.detachAllChildren();
    }

    public void fitView() {
        fit();
        centerView();
    }

    private void fit() {
        if (bounds == null) {
            return;
        }

        Transform trans = new Transform(objTrans.getTransform());
        BoundingBox tBounds = new BoundingBox();
        bounds.transform(trans, tBounds);

        Vector3[] corners = tBounds.getCorners(null);

        int xmax = -Integer.MAX_VALUE;
        int xmin = Integer.MAX_VALUE;
        int ymax = -Integer.MAX_VALUE;
        int ymin = Integer.MAX_VALUE;

        Camera cam = canvas.getCanvasRenderer().getCamera();

        for (int ii = 0; ii < 8; ii++) {
            Vector3 point = cam.getScreenCoordinates(corners[ii]);
            xmax = Math.max(xmax, (int) Math.round(point.getX()));
            xmin = Math.min(xmin, (int) Math.round(point.getX()));
            ymax = Math.max(ymax, (int) Math.round(point.getY()));
            ymin = Math.min(ymin, (int) Math.round(point.getY()));
        }

        double scalex = (xmax - xmin) / (double) canvas.getSurfaceWidth();
        double scaley = (ymax - ymin) / (double) canvas.getSurfaceHeight();

        if (scalex == 0.0 && scaley == 0.0) {
            scalex = 1.0;
            scaley = 1.0;
        }

        objTrans.setScale(objTrans.getScale().getX() / Math.max(scalex, scaley));
    }

    /**
     * Centers everything in the graphics window.
     */
    private void centerView() {
        if (bounds == null) {
            return;
        }
        Transform trans = new Transform(objTrans.getTransform());
        BoundingBox tBounds = new BoundingBox();
        bounds.transform(trans, tBounds);
        objTrans.addTranslation(-tBounds.getCenter().getX(), -tBounds.getCenter().getY(), -tBounds.getCenter().getZ());
    }

    public void viewXYPlane() {
        Transform trans = new Transform();
        objTrans.setTransform(trans);
        fit();
        centerView();
    }

    public void viewXZPlane() {
        Transform trans = new Transform();
        Matrix3 rotXMat = new Matrix3();
        rotXMat.fromAngles(-Math.PI / 2.0, 0, 0);
        trans.setRotation(rotXMat);
        objTrans.setTransform(trans);
        fit();
        centerView();
    }

    public void viewYZPlane() {
        Transform trans = new Transform();
        Matrix3 rotMat = new Matrix3();
        rotMat.fromAngles(-Math.PI / 2.0, -Math.PI / 2.0, 0);
        trans.setRotation(rotMat);
        objTrans.setTransform(trans);
        fit();
        centerView();
    }

    public void viewDiagonal() {
        Matrix3 rotY = new Matrix3();
        rotY.fromAngles(0, -3.0 * Math.PI / 4.0, 0);
        Matrix3 rotX = new Matrix3();
        rotX.fromAngles(-Math.PI / 2.0, 0, 0);
        Matrix3 rotX2 = new Matrix3();
        rotX2.fromAngles(Math.PI / 8.0, 0, 0);
        Matrix3 transMat = new Matrix3();
        transMat.multiplyLocal(rotX2);
        transMat.multiplyLocal(rotY);
        transMat.multiplyLocal(rotX);
        Transform trans = new Transform();
        trans.setRotation(transMat);
        objTrans.setTransform(trans);
        fit();
        centerView();
    }

    public void useNetForCriterion(boolean use) {
        showWireframe = use;
        if (wireframeAllowed) {
            if (use) {
                objTrans.attachChild(wireframeRoot);
            } else {
                objTrans.detachChild(wireframeRoot);
            }
        }
        root.updateGeometricState(0);
    }

    public void setBackgroundImage(File path) {
        root.detachChild(background);
        if (path == null) {
            GradientBackground gbackground = new GradientBackground(canvas);
            gbackground.setColorButtom(View3DProperties.getDefault().getColor2());
            gbackground.setColorTop(View3DProperties.getDefault().getColor1());
            background = gbackground;
        } else {
            background = new ImageBackground(canvas);
            //((ImageBackground) background).setImage(ImageUtilities.loadImage("de/view3d/resources/HC_3.2-64_gesamt.jpg"));
            try {
                ((ImageBackground) background).setImage(ImageIO.read(path));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        root.attachChild(background);
    }

    public void exportQuadArrays(FileWriter fw) throws IOException {
        List<Spatial> e = geometryRoot.getChildren();
        int nP = 0;
        ArrayList<float[]> quads = new ArrayList<>(10);
        for (Spatial spat : e) {
            if (spat instanceof Mesh) {
                MeshData data = ((Mesh) spat).getMeshData();
                float[] points = new float[data.getVertexCount() * 3];
                data.getVertexBuffer().get(points);
                quads.add(points);
                nP += data.getVertexCount();
            }
        }

        if (nP == 0) {
            return;
        }

        fw.write("# vtk DataFile Version 4.0\n");
        fw.write("eLamX2\n");
        fw.write("ASCII\n");
        fw.write("DATASET UNSTRUCTURED_GRID\n");
        fw.write("POINTS " + nP + " double\n");
        for (float[] fA : quads) {
            for (int ii = 0; ii < fA.length / 3; ii++) {
                fw.write("" + fA[ii * 3] + " " + fA[ii * 3 + 1] + " " + fA[ii * 3 + 2] + "\n");
            }
        }

        int numCells = nP / 4;
        fw.write("CELLS " + numCells + " " + numCells * 5 + "\n");
        for (int ii = 0; ii < numCells; ii++) {
            fw.write("4 " + (ii * 4) + " " + (ii * 4 + 1) + " " + (ii * 4 + 2) + " " + (ii * 4 + 3) + "\n");
        }

        fw.write("CELL_TYPES " + numCells + "\n");
        for (int ii = 0; ii < numCells; ii++) {
            fw.write("9\n");
        }
    }

    HashMap<AdditionalGeometryButton, Spatial> geoMap = new HashMap<>();

    private void updateAdditionalGeometry() {
        addGeometryRoot.removeFromParent();
        addGeometryRoot.detachAllChildren();
        for (AdditionalGeometryButton button : geoMap.keySet()) {
            if (button.isSelected()) {
                Spatial group = geoMap.get(button);
                if (group != null) {
                    addGeometryRoot.attachChild(group);
                }
            }
        }
        objTrans.attachChild(addGeometryRoot);
        root.updateGeometricState(0);
    }

    public static class AdditionalGeometryButton extends JToggleButton {

        private final View3D v3D;

        public AdditionalGeometryButton(View3D v3D) {
            super();
            this.v3D = v3D;
            this.addActionListener((ActionEvent e) -> {
                AdditionalGeometryButton.this.v3D.updateAdditionalGeometry();
            });
            setEnabled(false);
        }

        public void setGeo(Spatial geo) {
            v3D.geoMap.put(this, geo);
            v3D.updateAdditionalGeometry();
            setEnabled(geo != null);
        }
    }
}
