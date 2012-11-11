package mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import maps.IntIntSetMap;

import blocklists.DoubleBlockArrayList;
import blocklists.IntBlockArrayList;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

public class Mesh
{
	private final ArrayList<Vertex> mVertices = new ArrayList<Vertex>();
	private final ArrayList<Triangle> mTriangles = new ArrayList<Triangle>();
	private final HashSet<Edge> mEdges = new HashSet<Edge>();

	private boolean mEdgesUpToDate = true;

	private Random rnd = new Random();

	public Mesh()
	{
		super();
	}

	public Vertex addVertex(final double x, final double y)
	{
		final Vertex lVertex = new Vertex(x, y);
		mVertices.add(lVertex);
		return lVertex;
	}

	public Triangle addTriangle(final Vertex u,
															final Vertex v,
															final Vertex w)
	{
		Triangle lTriangle = new Triangle(u, v, w);
		mTriangles.add(lTriangle);
		mEdgesUpToDate = false;
		return lTriangle;
	}

	public void addVertexDelauney(double pX, double pY)
	{
		final Triangle lTriangle = findTriangleStrictlyContaining(pX, pY);
		if (lTriangle != null && !lTriangle.isVertex(pX, pY))
		{
			final Triangle[] lTriangleArray = breakupTriangle(lTriangle,
																												pX,
																												pY);

			//delauneyFlipEdgesOfTriangle(lTriangle);
			delauneyFlipEdgesOfTriangle(lTriangleArray[0]);
			delauneyFlipEdgesOfTriangle(lTriangleArray[1]);
			delauneyFlipEdgesOfTriangle(lTriangleArray[2]);/**/
		}
		else
		{
			System.out.format("Cannot insert (%g,%g) \n", pX, pY);
		}
	}

	private Triangle findTriangleContaining(double pX, double pY)
	{
		for (Triangle Triangle : mTriangles)
		{
			final boolean isInside = Triangle.isInside(pX, pY);
			if (isInside)
			{
				return Triangle;
			}
		}
		return null;
	}
	
	private Triangle findTriangleStrictlyContaining(double pX, double pY)
	{
		for (Triangle Triangle : mTriangles)
		{
			final boolean isInside = Triangle.isInside(pX, pY);
			if (isInside)
			{
				return Triangle;
			}
		}
		return null;
	}

	public void removeTriangle(Triangle pTriangle)
	{
		pTriangle.unlink();
		mTriangles.remove(pTriangle);
		mEdgesUpToDate = false;
	}

	public final void delauneyRelax(int pI)
	{
		final HashSet<Edge> lEdges = getEdges();
		for (Edge lEdge : lEdges)
		{
			delauneyFlipEdge(lEdge);
		}
	}

	private void delauneyFlipEdgesOfTriangle(Triangle pTriangle)
	{
		delauneyFlipEdge(pTriangle.u, pTriangle.v);
		delauneyFlipEdge(pTriangle.v, pTriangle.w);
		delauneyFlipEdge(pTriangle.w, pTriangle.u);
	}

	private final void delauneyFlipEdge(final Edge pEdge)
	{
		delauneyFlipEdge(pEdge.u, pEdge.v);
	}

	public final void delauneyFlipEdge(final Vertex u, final Vertex v)
	{
		final List<Triangle> lTwoTriangles = getTwoTriangles(u, v);
		if (lTwoTriangles.size() == 2)
		{
			final Triangle t1 = lTwoTriangles.get(0);
			final Triangle t2 = lTwoTriangles.get(1);

			final Vertex o1 = getOtherVertex(t1, u, v);
			final Vertex o2 = getOtherVertex(t2, u, v);

			final double alpha = Math.abs(Vertex.angle(o1, u, v));
			final double beta = Math.abs(Vertex.angle(o2, u, v));

			if (alpha + beta > Math.PI)
			{
				removeTriangle(t1);
				removeTriangle(t2);

				addTriangle(o1, o2, u);
				addTriangle(o1, o2, v);
			}
		}
	}

	public void flipEdge(Vertex u, Vertex v)
	{
		final List<Triangle> lTwoTriangles = getTwoTriangles(u, v);
		if (lTwoTriangles.size() == 2)
		{
			final Triangle t1 = lTwoTriangles.get(0);
			final Triangle t2 = lTwoTriangles.get(1);

			final Vertex o1 = getOtherVertex(t1, u, v);
			final Vertex o2 = getOtherVertex(t2, u, v);

			removeTriangle(t1);
			removeTriangle(t2);

			addTriangle(o1, o2, u);
			addTriangle(o1, o2, v);
		}
	}

	public final void breakEdge(final Vertex u,
															final Vertex v,
															final double pCutx,
															final double pCuty)
	{
		final List<Triangle> lTwoTriangles = getTwoTriangles(u, v);
		if (lTwoTriangles.size() == 2)
		{
			final Triangle t1 = lTwoTriangles.get(0);
			final Triangle t2 = lTwoTriangles.get(1);

			final Vertex o1 = getOtherVertex(t1, u, v);
			final Vertex o2 = getOtherVertex(t2, u, v);

			removeTriangle(t1);
			removeTriangle(t2);

			final Vertex w = addVertex(pCutx, pCuty);

			addTriangle(w, u, o1);
			addTriangle(w, v, o1);
			addTriangle(w, u, o2);
			addTriangle(w, v, o2);
		}
	}

	public void breakEdgeInMiddle(Vertex pU, Vertex pV)
	{
		final double x = 0.5 * (pU.x + pV.x);
		final double y = 0.5 * (pU.y + pV.y);

		breakEdge(pU, pV, x, y);
	}

	public final List<Triangle> getTwoTriangles(final Vertex u,
																							final Vertex v)
	{
		if (Vertex.areNeighbours(u, v))
		{

			final ArrayList<Triangle> t1s = u.getTriangles();
			final ArrayList<Triangle> t2s = v.getTriangles();

			final ArrayList<Triangle> intersection = new ArrayList<Triangle>(t1s);
			intersection.retainAll(t2s);
			return intersection;
		}
		else
			return Collections.emptyList();
	}

	private Triangle[] breakupTriangle(	Triangle pTriangle,
																			double pX,
																			double pY)
	{
		removeTriangle(pTriangle);

		Vertex lVertex = addVertex(pX, pY);

		Triangle lTriangle1 = addTriangle(pTriangle.u,
																			pTriangle.v,
																			lVertex);
		Triangle lTriangle2 = addTriangle(pTriangle.v,
																			pTriangle.w,
																			lVertex);
		Triangle lTriangle3 = addTriangle(pTriangle.w,
																			pTriangle.u,
																			lVertex);

		Triangle[] lTriangles = new Triangle[]
		{ lTriangle1, lTriangle2, lTriangle3 };
		return lTriangles;
	}

	public final void breakupTriangle(final Triangle t,
																		final double a,
																		final double b,
																		final double c)
	{
		final Vertex A = t.u;
		final Vertex B = t.v;
		final Vertex C = t.w;

		final Triangle tC = getOpposedTriangle(t, A, B);
		final Triangle tA = getOpposedTriangle(t, B, C);
		final Triangle tB = getOpposedTriangle(t, C, A);

		final Vertex Cp = getOtherVertex(tC, A, B);
		final Vertex Ap = getOtherVertex(tA, B, C);
		final Vertex Bp = getOtherVertex(tB, C, A);

		final double Appx = (1 - a) * A.x + a * B.x;
		final double Appy = (1 - a) * A.y + a * B.y;

		final double Bppx = (1 - b) * B.x + b * C.x;
		final double Bppy = (1 - b) * B.y + b * C.y;

		final double Cppx = (1 - c) * C.x + c * A.x;
		final double Cppy = (1 - c) * C.y + c * A.y;

		removeTriangle(t);
		removeTriangle(tA);
		removeTriangle(tB);
		removeTriangle(tC);

		final Vertex App = addVertex(Appx, Appy);
		final Vertex Bpp = addVertex(Bppx, Bppy);
		final Vertex Cpp = addVertex(Cppx, Cppy);

		addTriangle(Cp, App, A);
		addTriangle(Cp, App, B);

		addTriangle(Ap, Bpp, B);
		addTriangle(Ap, Bpp, C);

		addTriangle(Bp, Cpp, C);
		addTriangle(Bp, Cpp, A);

		addTriangle(App, A, Cpp);
		addTriangle(App, B, Bpp);

		addTriangle(Bpp, B, App);
		addTriangle(Bpp, C, Cpp);

		addTriangle(Cpp, C, Bpp);
		addTriangle(Cpp, A, App);

		addTriangle(App, Bpp, Cpp);

	}

	private final Vertex getOtherVertex(final Triangle t,
																			final Vertex u,
																			final Vertex v)
	{

		final Vertex a = t.u;
		final Vertex b = t.v;
		final Vertex c = t.w;
		if (u == a)
		{
			if (v == b)
			{
				return c;
			}
			else if (v == c)
			{
				return b;
			}
		}
		else if (u == b)
		{
			if (v == a)
			{
				return c;
			}
			else if (v == c)
			{
				return a;
			}
		}
		else if (u == c)
		{
			if (v == a)
			{
				return b;
			}
			else if (v == b)
			{
				return a;
			}
		}

		return null;
	}

	public final Triangle getOpposedTriangle(	final Triangle t,
																						final Vertex u,
																						final Vertex v)
	{
		final ArrayList<Triangle> t1s = u.getTriangles();
		final ArrayList<Triangle> t2s = v.getTriangles();

		final ArrayList<Triangle> intersection = new ArrayList<Triangle>(t1s);
		intersection.remove(t);
		intersection.retainAll(t2s);
		if (intersection.size() == 1)
			return intersection.get(0);
		else
			return null;
	}

	public ArrayList<Vertex> getNeighbours(final Vertex v)
	{
		return v.getNeighbours();
	}

	public void initialize(final int pSize)
	{
		final Vertex[][] vertices = new Vertex[pSize][pSize];

		final double step = (double) 1 / (pSize - 1);
		for (int i = 0; i < pSize; i++)
		{
			for (int j = 0; j < pSize; j++)
			{
				final double x = step * i;
				final double y = step * j;

				vertices[i][j] = addVertex(x, y);
			}
		}

		for (int i = 0; i < pSize - 1; i++)
		{
			for (int j = 0; j < pSize - 1; j++)
			{
				if (i % 2 == 0)
				{
					addTriangle(vertices[i][j],
											vertices[i + 1][j],
											vertices[i][j + 1]);
					addTriangle(vertices[i + 1][j + 1],
											vertices[i + 1][j],
											vertices[i][j + 1]);
				}
				else
				{
					addTriangle(vertices[i][j],
											vertices[i + 1][j],
											vertices[i + 1][j + 1]);
					addTriangle(vertices[i][j],
											vertices[i][j + 1],
											vertices[i + 1][j + 1]);
				}
			}
		}

	}

	public final int getNumberOfVertices()
	{
		return mVertices.size();
	}

	public final int getNumberOfTriangles()
	{
		return mTriangles.size();
	}

	public final ArrayList<Triangle> getTriangles()
	{
		return mTriangles;
	}

	public ArrayList<Vertex> getVertices()
	{
		return mVertices;
	}

	public Vertex getRandomVertex()
	{
		return mVertices.get(rnd.nextInt(mVertices.size()));
	}

	public Vertex getRandomNeighbourOf(Vertex pVertex)
	{
		ArrayList<Vertex> lNeighbours = pVertex.getNeighbours();
		return lNeighbours.get(rnd.nextInt(lNeighbours.size()));
	}

	public final HashSet<Edge> getEdges()
	{
		if (!mEdgesUpToDate)
		{
			mEdges.clear();
			for (Vertex lVertex : getVertices())
			{
				for (Vertex lNeighbour : lVertex.getNeighbours())
				{
					final Edge lEdge = new Edge(lVertex, lNeighbour);
					mEdges.add(lEdge);
				}
			}
			mEdgesUpToDate = true;
		}
		return mEdges;
	}

}
