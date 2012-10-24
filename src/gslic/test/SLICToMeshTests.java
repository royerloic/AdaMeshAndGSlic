package gslic.test;

import utils.graphics.hyperplane.HyperPlaneRenderer;
import utils.graphics.hyperplane.Node;
import utils.graphics.hyperplane.graphics.GraphicsProvider;
import utils.graphics.hyperplane.graphics.LWJGLGraphics;
import utils.graphics.hyperplane.humaninterface.HumanInterfaceTranslatorInterface;
import utils.graphics.hyperplane.humaninterface.HumanInterfaceTranslatorTouchPad;
import utils.graphics.hyperplane.primitives.Image;
import utils.graphics.hyperplane.primitives.LineMesh;
import utils.graphics.hyperplane.rendermodules.ImagesBasic;
import utils.graphics.hyperplane.rendermodules.RenderModuleInterface;
import utils.graphics.hyperplane.rendermodules.TriangleMeshVertexArrays;
import gslic.GeodesicSLIC;
import gslic.LabelFieldToMesh;
import gslic.LaplacianGeodesicSLIC;
import gslic.SLICAbstract;
import gslic.SLICVisualization;
import gslic.StandardSLIC;
import iamesh.IAMAlgorythm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import mesh.Mesh;
import mesh.MeshVisualization;
import mesh.Triangle;
import mesh.Vertex;

import org.junit.Test;
import org.lwjgl.LWJGLException;

import blocklists.DoubleBlockArrayList;

import utils.concurency.thread.EnhancedThread;
import utils.image.DoubleArrayImage;
import utils.opengl.Color;

public class SLICToMeshTests
{
	Random rnd = new Random();

	@Test
	public void testSLIC() throws LWJGLException,
												IOException,
												InstantiationException,
												IllegalAccessException
	{
		final InputStream lInputStream = SLICToMeshTests.class.getResourceAsStream("/image/tail1.1024.png");
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

		LaplacianGeodesicSLIC lSLIC = new LaplacianGeodesicSLIC(lDoubleImage,
																														128,
																														0.01,
																														3,
																														1);
		SLICVisualization lSLIClaplVisualization = new SLICVisualization(lSLIC);
		// lSLICnewVisualization.setDisplayImage(false);
		lSLIClaplVisualization.attachToNode(lNode1);

		lSLIC.initialize();
		lSLIC.steps(15);
		lSLIC.filterLabelField(1);
		lSLIC.filterOutSmallComponents(0.25);

		lSLIClaplVisualization.updateBoudaries(Color.red);
		lSLIClaplVisualization.updateCentroids();

		DoubleArrayImage lLaplacianDoubleImage = lSLIC.getEdgeImage();
		RenderModuleInterface lImageRenderModule = lNode2.getRenderModule(ImagesBasic.class);
		final Image lLaplacianImage = new Image(0,
																						0,
																						0.5,
																						1,
																						1,
																						lLaplacianDoubleImage.getMonochromeByteBuffer(-0.125,
																																										0.125),
																						lLaplacianDoubleImage.getWidth(),
																						lLaplacianDoubleImage.getHeight(),
																						true,
																						true);
		lImageRenderModule.add(lLaplacianImage);

		Mesh lMesh = LabelFieldToMesh.labelFieldToMesh(	lSLIC.getLabelField(),
																										lDoubleImage.getWidth(),
																										lDoubleImage.getHeight());

		MeshVisualization lMeshVisualization = new MeshVisualization(	lMesh,
																																	Color.blue);
		lMeshVisualization.attachToNode(lNode2);
		lMeshVisualization.updateMesh();

		lHyperPlaneRenderer.start();
		lHyperPlaneRenderer.waitForRunning();
		lHyperPlaneRenderer.waitToFinish();

	}

}
