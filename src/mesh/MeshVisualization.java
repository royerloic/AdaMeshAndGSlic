package mesh;

import utils.opengl.Color;
import utils.graphics.hyperplane.Node;
import utils.graphics.hyperplane.primitives.LineMesh;
import utils.graphics.hyperplane.primitives.Point;
import utils.graphics.hyperplane.rendermodules.ImagesBasic;
import utils.graphics.hyperplane.rendermodules.PointsBasic;
import utils.graphics.hyperplane.rendermodules.RenderModuleInterface;
import utils.graphics.hyperplane.rendermodules.TriangleMeshVertexArrays;

public class MeshVisualization
{

	private final Mesh mMesh;

	private Color mMeshColor;
	private final LineMesh mLineMesh;
	private RenderModuleInterface<LineMesh> mLineMeshRenderModule;

	public MeshVisualization(Mesh pMesh, Color pMeshColor)
	{
		mMesh = pMesh;
		mMeshColor = pMeshColor;
		mLineMesh = new LineMesh();
	}

	public void attachToNode(final Node pNode) throws InstantiationException,
																						IllegalAccessException
	{
		mLineMeshRenderModule = pNode.getRenderModule(TriangleMeshVertexArrays.class);
		mLineMeshRenderModule.add(mLineMesh);
	}

	public void updateMesh()
	{

		int i = 0;
		mLineMesh.clearVertices();
		for (final Vertex lVertex : mMesh.getVertices())
		{
			final double x = lVertex.x;
			final double y = lVertex.y;
			lVertex.i = i;

			mLineMesh.setVertex(i, x, y, 1);
			mLineMesh.setColor(i, 1, 0, 0, 1);
			i++;
		}

		mLineMesh.clearLines();
		for (final Triangle lTriangle : mMesh.getTriangles())
		{
			final int ui = lTriangle.u.i;
			final int vi = lTriangle.v.i;
			final int wi = lTriangle.w.i;

			mLineMesh.addLine(ui, vi);
			mLineMesh.addLine(vi, wi);
			mLineMesh.addLine(wi, ui);
		}
		mLineMesh.requestUpdate();
	}

}
