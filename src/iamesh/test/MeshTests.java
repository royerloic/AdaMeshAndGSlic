package iamesh.test;

import utils.graphics.hyperplane.HyperPlaneRenderer;
import utils.graphics.hyperplane.Node;
import utils.graphics.hyperplane.graphics.GraphicsProvider;
import utils.graphics.hyperplane.graphics.LWJGLGraphics;
import utils.graphics.hyperplane.humaninterface.HumanInterfaceTranslatorInterface;
import utils.graphics.hyperplane.humaninterface.HumanInterfaceTranslatorTouchPad;

import gslic.test.SLICToMeshTests;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import mesh.Mesh;
import mesh.MeshVisualization;
import mesh.Vertex;

import org.junit.Test;
import org.lwjgl.LWJGLException;

import utils.image.DoubleArrayImage;
import utils.opengl.Color;

public class MeshTests
{
	Random rnd = new Random();

	@Test
	public void test() throws LWJGLException, IOException, InstantiationException, IllegalAccessException
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
		

		final Mesh lMesh = new Mesh();
		
		final Vertex a = lMesh.addVertex(0, 0);
		final Vertex b = lMesh.addVertex(0, 1);
		final Vertex c = lMesh.addVertex(1, 1);
		final Vertex d = lMesh.addVertex(1, 0);
		
		lMesh.addTriangle(a, b, c);
		lMesh.addTriangle(a, d, c);
		
		for(int i=0; i<10; i++)
		{
			lMesh.addVertexDelauney(rnd.nextDouble(), rnd.nextDouble());
		}
		
		lMesh.delauneyRelax(100000);
	

		MeshVisualization lMeshVisualization = new MeshVisualization(lMesh,Color.red);
		lMeshVisualization.attachToNode(lNode1);
		lMeshVisualization.updateMesh();
		
		lHyperPlaneRenderer.start();
		lHyperPlaneRenderer.waitForRunning();
		lHyperPlaneRenderer.waitToFinish();
	}

}
