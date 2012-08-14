package gslic.test;

import utils.graphics.hyperplane.HyperPlaneRenderer;
import utils.graphics.hyperplane.Node;
import utils.graphics.hyperplane.graphics.GraphicsProvider;
import utils.graphics.hyperplane.graphics.LWJGLGraphics;
import utils.graphics.hyperplane.humaninterface.HumanInterfaceTranslatorInterface;
import utils.graphics.hyperplane.humaninterface.HumanInterfaceTranslatorTouchPad;
import utils.graphics.hyperplane.primitives.Image;
import utils.graphics.hyperplane.rendermodules.ImagesBasic;
import utils.graphics.hyperplane.rendermodules.RenderModuleInterface;

import gslic.GeodesicSLIC;
import gslic.LaplacianGeodesicSLIC;
import gslic.SLICAbstract;
import gslic.SLICVisualization;
import gslic.StandardSLIC;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.junit.Test;
import org.lwjgl.LWJGLException;

import utils.image.DoubleArrayImage;
import utils.opengl.Color;

public class SLICTests
{
	Random rnd = new Random();

	@Test
	public void testSLIC() throws LWJGLException,
												IOException,
												InstantiationException,
												IllegalAccessException
	{
		final int lResolution =256;
		final InputStream lInputStream = SLICTests.class.getResourceAsStream("/image/tail1.1024.png");
		final DoubleArrayImage lDoubleImage = new DoubleArrayImage(lInputStream);

		final GraphicsProvider lLWJGLGraphics = LWJGLGraphics.fakefullscreen();
		final HumanInterfaceTranslatorInterface lHumanInterfaceTranslator = new HumanInterfaceTranslatorTouchPad(lLWJGLGraphics);

		final HyperPlaneRenderer lHyperPlaneRenderer = new HyperPlaneRenderer(lLWJGLGraphics,
																																					lHumanInterfaceTranslator);
		lHyperPlaneRenderer.setBackgroundColor(Color.white);

		final Node lRootNode = lHyperPlaneRenderer.getRootNode();
		;

		Node lNode1 = lRootNode.addChild(0, 0, 0.4, 0.4);
		Node lNode2 = lRootNode.addChild(0.5, 0, 0.4, 0.4);
		Node lNode3 = lRootNode.addChild(0, 0.5, 0.4, 0.4);
		Node lNode4 = lRootNode.addChild(0.5, 0.5, 0.4, 0.4);

		SLICAbstract lSLICorg = new StandardSLIC(lDoubleImage, lResolution, 0.01);
		SLICVisualization lSLICorgVisualization = new SLICVisualization(lSLICorg);
		lSLICorgVisualization.attachToNode(lNode1);

		lSLICorg.initialize();
		lSLICorg.steps(15);
		lSLICorg.filterLabelField(1);
		lSLICorg.filterOutSmallComponents(0.25);

		lSLICorgVisualization.updateBoudaries(Color.red);
		lSLICorgVisualization.updateCentroids();

		SLICAbstract lSLICnew = new GeodesicSLIC(lDoubleImage, lResolution, 0.001);
		SLICVisualization lSLICnewVisualization = new SLICVisualization(lSLICnew);
		// lSLICnewVisualization.setDisplayImage(false);
		lSLICnewVisualization.attachToNode(lNode2);

		lSLICnew.initialize();
		lSLICnew.steps(15);
		lSLICnew.filterLabelField(1);
		lSLICnew.filterOutSmallComponents(0.25);

		lSLICnewVisualization.updateBoudaries(Color.red);
		lSLICnewVisualization.updateCentroids();

		LaplacianGeodesicSLIC lSLIClapl = new LaplacianGeodesicSLIC(	lDoubleImage,
		                                                            	lResolution,
																												0.1,
																												5,
																												1);
		SLICVisualization lSLIClaplVisualization = new SLICVisualization(lSLIClapl);
		// lSLICnewVisualization.setDisplayImage(false);
		lSLIClaplVisualization.attachToNode(lNode3);

		lSLIClapl.initialize();
		lSLIClapl.steps(50);
		lSLIClapl.filterLabelField(1);
		lSLIClapl.filterOutSmallComponents(0.25);

		lSLIClaplVisualization.updateBoudaries(Color.red);
		lSLIClaplVisualization.updateCentroids();

		DoubleArrayImage lLaplacianDoubleImage = lSLIClapl.getEdgeImage();
		RenderModuleInterface lImageRenderModule = lNode4.getRenderModule(ImagesBasic.class);
		final Image lLaplacianImage = new Image(0,
																						0,
																						0.5,
																						1,
																						1,
																						lLaplacianDoubleImage.getRGBByteBuffer(-1,
																																										1),
																						lLaplacianDoubleImage.getWidth(),
																						lLaplacianDoubleImage.getHeight(),
																						true);
		//lImageRenderModule.add(lLaplacianImage);

		lHyperPlaneRenderer.start();
		lHyperPlaneRenderer.waitForRunning();
		lHyperPlaneRenderer.waitToFinish();

	}

}
