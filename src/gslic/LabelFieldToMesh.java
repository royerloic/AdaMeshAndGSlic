package gslic;

import mesh.Mesh;
import mesh.Vertex;

public class LabelFieldToMesh
{

	public static final Mesh labelFieldToMesh(final int[] pIs,
																						final int pWidth,
																						final int pHeight)
	{
		//TODO: do something with pWidth and pHeight
		final Mesh lMesh = new Mesh();

		final Vertex a = lMesh.addVertex(0, 0);
		final Vertex b = lMesh.addVertex(0, 1);
		final Vertex c = lMesh.addVertex(1, 1);
		final Vertex d = lMesh.addVertex(1, 0);

		lMesh.addTriangle(a, b, c);
		lMesh.addTriangle(a, d, c);

		//TODO: fix this, I commented the line below, what was I doing here?
		//addVertices(lMesh, pIs);

		lMesh.delauneyRelax((int) Math.sqrt(lMesh.getNumberOfTriangles()));
		return lMesh;
	}

	public static final void addVertices(	final Mesh pMesh,
																				final LabelField pLabelField)
	{
		final int[] lLabelFieldArray = pLabelField.array;
		final int width = pLabelField.width;
		final int height = pLabelField.height;

		for (int xi = 1; xi < width; xi++)
		{
			System.out.format("%d \n", xi);
			for (int yi = 1; yi < height; yi++)
			{

				final double x = ((double) xi) / width;
				final double y = ((double) yi) / height;
				if (isJunctionPoint(lLabelFieldArray, width, xi, yi))
				{
					add(pMesh, x, y);
				}
			}
		}
	}

	private static void add(final Mesh pMesh, final double pX, double pY)
	{
		pMesh.addVertexDelauney(pX, pY);
	}

	private static boolean isJunctionPoint(	final int[] pLabelField,
																					final int pWidth,
																					final int pX,
																					final int pY)
	{
		final int lOffset = pX + pWidth * pY;
		final int a = pLabelField[lOffset];
		final int b = pLabelField[lOffset - pWidth];
		final int c = pLabelField[lOffset - pWidth - 1];
		final int d = pLabelField[lOffset - 1];

		final int lCount = (a != b ? 1 : 0) + (b != c ? 1 : 0)
												+ (c != d ? 1 : 0)
												+ (d != a ? 1 : 0);

		final boolean lISJunctionPoint = lCount >= 3;

		return lISJunctionPoint;
	}

}
