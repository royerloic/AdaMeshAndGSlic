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
import iamesh.SimpleAdaptiveMesh;

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

public class SimpleAdaptiveMeshTests
{
	Random rnd = new Random();

	@Test
	public void testSimpleImage() throws LWJGLException, IOException
	{
		final InputStream lInputStream = SimpleAdaptiveMeshTests.class.getResourceAsStream("../../image/testimage.2048.png");
		final DoubleArrayImage lDoubleImage = new DoubleArrayImage(lInputStream);

		final GraphicsProvider lLWJGLGraphics = LWJGLGraphics.fakefullscreen();
		final HumanInterfaceTranslatorInterface lHumanInterfaceTranslator = new HumanInterfaceTranslatorTouchPad(lLWJGLGraphics);

		final HyperPlaneRenderer lHyperPlaneRenderer = new HyperPlaneRenderer(lLWJGLGraphics,
																																					lHumanInterfaceTranslator);

		final Node lRootNode = lHyperPlaneRenderer.getRootNode();

		final byte[] lArray = new byte[lDoubleImage.getLength() * 4];

		lDoubleImage.getRGBByteBuffer(0, 1).get(lArray, 0, lArray.length);

		final Node lChild = lRootNode;

		final ImagesBasic lImagesBasic = new ImagesBasic();
		lChild.addRenderModule(lImagesBasic);
		final Image lImage = new Image(	0,
																		0,
																		0.5,
																		1,
																		1,
																		lDoubleImage,
																		0,
																		1);
		lImage.linear = false;
		lImage.hflip = true;
		lImagesBasic.add(lImage);

		final TriangleMeshVertexArrays lTriangleMeshVertexArrays = new TriangleMeshVertexArrays();
		lChild.addRenderModule(lTriangleMeshVertexArrays);

		final LineMesh lLineMesh = new LineMesh();
		lTriangleMeshVertexArrays.add(lLineMesh);

		final LineMesh lFeatureLines = new LineMesh();
		lTriangleMeshVertexArrays.add(lFeatureLines);

		final Mesh lAdaptiveMesh = new Mesh();

		final Vertex a = lAdaptiveMesh.addVertex(0, 0);
		final Vertex b = lAdaptiveMesh.addVertex(0, 1);
		final Vertex c = lAdaptiveMesh.addVertex(1, 1);
		final Vertex d = lAdaptiveMesh.addVertex(1, 0);

		lAdaptiveMesh.addTriangle(a, b, c);
		lAdaptiveMesh.addTriangle(a, d, c);

		final Mesh lAnalysingMesh = new Mesh();
		lAnalysingMesh.initialize(128);

		final SimpleAdaptiveMesh lSimpleAdaptiveMesh = new SimpleAdaptiveMesh(lDoubleImage,
																																	lAnalysingMesh,
																																	lAdaptiveMesh);

		lSimpleAdaptiveMesh.clearFlags();
		lSimpleAdaptiveMesh.setOneRandomFlag(true);
		lSimpleAdaptiveMesh.relaxall(1);
		lSimpleAdaptiveMesh.relaxall(1);
		lSimpleAdaptiveMesh.clearFlags();

		lSimpleAdaptiveMesh.computeFeaturePoints(128, 0.000001, 5);

		lAdaptiveMesh.delauneyRelax(10000000);

		// copyMeshToDisplay(lMesh, lLineMesh);

		lHyperPlaneRenderer.start();
		lHyperPlaneRenderer.waitForRunning();

		for (int i = 0; i < 100000 && lHyperPlaneRenderer.isRunning(); i++)
		{
			//lSimpleAdaptiveMesh.clearFlags();
			//lSimpleAdaptiveMesh.delauney(1);
			lAdaptiveMesh.delauneyRelax(10000000);

			updateDisplay(lDoubleImage,
										lTriangleMeshVertexArrays,
										lLineMesh,
										lAdaptiveMesh);/**/
			updateFeaturePoints(lFeatureLines, lSimpleAdaptiveMesh);

		}

		lHyperPlaneRenderer.waitToFinish();

	}

	private void updateDisplay(	final DoubleArrayImage lDoubleImage,
															final TriangleMeshVertexArrays lTriangleMeshVertexArrays,
															final LineMesh lLineMesh,
															final Mesh lMesh)
	{
		copyMeshToDisplay(lMesh, lLineMesh);
		lLineMesh.requestUpdate();
	}

	private void updateFeaturePoints(	LineMesh pFeatureLines,
																		SimpleAdaptiveMesh pSimpleMesh)
	{
		try
		{
			pFeatureLines.clearVertices();
			pFeatureLines.clearLines();

			final DoubleBlockArrayList lFeaturePoints = pSimpleMesh.getFeaturePoints();
			final int lNumberOfFeaturePoints = lFeaturePoints.getNumberOfBlocks();
			for (int i = 0; i < lNumberOfFeaturePoints; i++)
			{
				final double x0 = lFeaturePoints.get(i, 0);
				final double y0 = lFeaturePoints.get(i, 1);

				pFeatureLines.addLine(pFeatureLines.addVertexAndColor(x0 + 0.0001,
																															y0 + 0.0001,
																															1,
																															Color.green),
															pFeatureLines.addVertexAndColor(x0,
																															y0,
																															1,
																															Color.blue));
			}
			pFeatureLines.requestUpdate();
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
