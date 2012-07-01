package iamesh.test;

import utils.graphics.hyperplane.HyperPlaneRenderer;
import utils.graphics.hyperplane.Node;
import utils.graphics.hyperplane.graphics.GraphicsProvider;
import utils.graphics.hyperplane.graphics.LWJGLGraphics;
import utils.graphics.hyperplane.humaninterface.HumanInterfaceTranslatorInterface;
import utils.graphics.hyperplane.humaninterface.HumanInterfaceTranslatorTouchPad;
import utils.graphics.hyperplane.primitives.Image;
import utils.graphics.hyperplane.primitives.LineMesh;
import utils.graphics.hyperplane.rendermodules.ImagesBasic;
import utils.graphics.hyperplane.rendermodules.TriangleMeshVertexArrays;
import iamesh.IAMAlgorythm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import mesh.Mesh;
import mesh.Triangle;
import mesh.Vertex;

import org.junit.Test;
import org.lwjgl.LWJGLException;

import utils.graphics.hyperplane.HyperPlaneRendererListener;
import utils.image.DoubleArrayImage;
import utils.opengl.Color;
import blocklists.DoubleBlockArrayList;

public class ImageAdaptiveMeshTests
{
	Random rnd = new Random();

	@Test
	public void testSimpleImage() throws LWJGLException, IOException
	{
		// final InputStream lInputStream =
		// ImageAdaptiveMeshTests.class.getResourceAsStream("image/testimage.median.2048.png");
		final InputStream lInputStream = ImageAdaptiveMeshTests.class.getResourceAsStream("../../image/tail1.2048.png");
		final DoubleArrayImage lDoubleImage = new DoubleArrayImage(lInputStream);

		// lDoubleImage.sympower(2);

		final GraphicsProvider lLWJGLGraphics = LWJGLGraphics.fakefullscreen();
		final HumanInterfaceTranslatorInterface lHumanInterfaceTranslator = new HumanInterfaceTranslatorTouchPad(lLWJGLGraphics);

		final HyperPlaneRenderer lHyperPlaneRenderer = new HyperPlaneRenderer(lLWJGLGraphics,
																																									lHumanInterfaceTranslator);

		final Node lRootNode = lHyperPlaneRenderer.getRootNode();

		final byte[] lArray = new byte[lDoubleImage.getLength() * 4];

		lDoubleImage.getGreyByteBuffer(0, 1)
								.get(lArray, 0, lArray.length);

		// Node lChild = lRootNode.addChild(0, 0, 1, 1);
		final Node lChild = lRootNode;

		final ImagesBasic lImagesBasic = new ImagesBasic();
		lChild.addRenderModule(lImagesBasic);
		final Image lImage = new Image(	0,
																		0,
																		0.5,
																		1,
																		1,
																		lDoubleImage.getGreyByteBuffer(	0,
																																		1),
																		lDoubleImage.getWidth(),
																		lDoubleImage.getHeight(),
																		true);
		lImage.linear = false;
		lImagesBasic.add(lImage);

		final TriangleMeshVertexArrays lTriangleMeshVertexArrays = new TriangleMeshVertexArrays();
		lChild.addRenderModule(lTriangleMeshVertexArrays);

		final LineMesh lLineMesh = new LineMesh();
		lTriangleMeshVertexArrays.add(lLineMesh);

		final LineMesh lFeatureLines = new LineMesh();
		lTriangleMeshVertexArrays.add(lFeatureLines);

		final Mesh lMesh = new Mesh();
		lMesh.initialize(64);

		final IAMAlgorythm lIAMAlgorythm = new IAMAlgorythm(lDoubleImage,
																												lMesh);

		lIAMAlgorythm.clearFlags();
		lIAMAlgorythm.setOneRandomFlag(true);
		lIAMAlgorythm.relaxall(1);
		lIAMAlgorythm.clearFlags();

		copyMeshToDisplay(lMesh, lLineMesh);

		lHyperPlaneRenderer.start();
		lHyperPlaneRenderer.waitForRunning();

		for (int i = 0; i < 100000 && lHyperPlaneRenderer.isRunning(); i++)
		{
			lIAMAlgorythm.clearFlags();

			final double factor = 1.0; // /(1+Math.log(i+1));
			System.out.println(i);

			// lIAMAlgorythm.relaxall(1);
			/*
			 * updateDisplay(lDoubleImage, lTriangleMeshVertexArrays, lLineMesh,
			 * lMesh); EnhancedThread.sleep(1000);/*
			 */

			/*
			 * updateDisplay(lDoubleImage, lTriangleMeshVertexArrays, lLineMesh,
			 * lMesh); EnhancedThread.sleep(1000);/*
			 */

			for (int r = 0; r < 5; r++)
			{
				lIAMAlgorythm.relaxall(1);
				lIAMAlgorythm.snap(32, 1, 0.1);
				lIAMAlgorythm.delauney(1);
			}

			lIAMAlgorythm.snap(32, 1, 0.00000001);
			lIAMAlgorythm.decompose(32, 0.00000001, 4);

			lIAMAlgorythm.snap(128, 1, 0.001);
			lIAMAlgorythm.delauney(10);
			// updateFeaturePoints(lFeatureLines, lIAMAlgorythm);
			updateDisplay(lDoubleImage,
										lTriangleMeshVertexArrays,
										lLineMesh,
										lMesh);
			// EnhancedThread.sleep(1000);

			/*
			 * lIAMAlgorythm.relaxbybreak(); updateDisplay(lDoubleImage,
			 * lTriangleMeshVertexArrays, lLineMesh, lMesh);
			 * EnhancedThread.sleep(1000);/*
			 */

			// lIAMAlgorythm.clearFlags();
			// lIAMAlgorythm.relaxRepeat(1, 1);

		}

		lHyperPlaneRenderer.waitToFinish();

	}

	private void updateDisplay(	final DoubleArrayImage lDoubleImage,
															final TriangleMeshVertexArrays lTriangleMeshVertexArrays,
															final LineMesh lLineMesh,
															final Mesh lMesh)
	{
		copyMeshToDisplay(lMesh, lLineMesh);
		lTriangleMeshVertexArrays.update();
	}

	private void updateFeaturePoints(	LineMesh pFeatureLines,
																		IAMAlgorythm pIAMAlgorythm)
	{
		try
		{
			pFeatureLines.clear();
			final DoubleBlockArrayList lFeaturePoints = pIAMAlgorythm.mFeaturePoints();
			final int lNumberOfFeaturePoints = lFeaturePoints.getNumberOfBlocks();
			for (int i = 0; i < lNumberOfFeaturePoints; i++)
			{
				final double x1 = lFeaturePoints.get(i, 0);
				final double y1 = lFeaturePoints.get(i, 1);
				final double x2 = lFeaturePoints.get(i, 2);
				final double y2 = lFeaturePoints.get(i, 3);
				pFeatureLines.addLine(pFeatureLines.addVertexAndColor(x1,
																															y1,
																															1,
																															Color.green),
															pFeatureLines.addVertexAndColor(x2,
																															y2,
																															1,
																															Color.blue));
			}
		}
		catch (final Throwable e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void copyMeshToDisplay(Mesh lMesh, LineMesh lLineMesh)
	{

		int i = 0;
		lLineMesh.clearVertices();
		for (final Vertex lVertex : lMesh.getVertices())
		{
			final double x = lVertex.x;
			final double y = lVertex.y;
			lVertex.i = i;

			lLineMesh.setVertex(i, x, y, 1);
			lLineMesh.setColor(i, 1, 0, 0, 1);
			i++;
		}

		lLineMesh.clearLines();
		for (final Triangle lTriangle : lMesh.getTriangles())
		{
			final int ui = lTriangle.u.i;
			final int vi = lTriangle.v.i;
			final int wi = lTriangle.w.i;

			lLineMesh.addLine(ui, vi);
			lLineMesh.addLine(vi, wi);
			lLineMesh.addLine(wi, ui);
		}

	}

}
