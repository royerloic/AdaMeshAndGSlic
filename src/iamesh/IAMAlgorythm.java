package iamesh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import mesh.Edge;
import mesh.Mesh;
import mesh.Triangle;
import mesh.Vertex;
import utils.image.DoubleArrayImage;
import utils.image.linearfeatures.LinearFeatures;
import blocklists.DoubleBlockArrayList;

public class IAMAlgorythm
{
	private final DoubleArrayImage mDoubleImage;
	private final Mesh mMesh;

	final HashSet<Vertex> mChangedVerticesSet;

	DoubleBlockArrayList mFeaturePoints = new DoubleBlockArrayList(4);
	private final Random rnd = new Random();
	private double mThresholdRatio = 0.2;
	private double mThresholdSupport = 0.2;

	ArrayList<Edge> mStressedEdges = new ArrayList<Edge>();
	private double mMaximalAllowedStress = 0.001;

	public IAMAlgorythm(DoubleArrayImage pDoubleImage,
											Mesh pImageAdaptiveMesh)
	{
		super();
		mDoubleImage = pDoubleImage;
		mMesh = pImageAdaptiveMesh;
		mChangedVerticesSet = new HashSet<Vertex>();
	}

	public void adapt(final int pSteps)
	{
		for (int i = 0; i < pSteps; i++)
		{
			step();
		}
	}

	public void step()
	{

	};

	public void relaxRepeat(final int pRepeats, final double pFactor)
	{
		for (int i = 0; i < pRepeats; i++)
		{
			final HashSet<Vertex> lChangedVerticesSet = new HashSet<Vertex>(mChangedVerticesSet);

			while (lChangedVerticesSet.addAll(relax(lChangedVerticesSet,
																							pFactor)))
			{
			}
		}
	}

	public ArrayList<Vertex> relax(	final HashSet<Vertex> pChangedVerticesSet,
																	final double pFactor)
	{
		ArrayList<Vertex> lNewFixedVerticesSet = new ArrayList<Vertex>();

		for (Vertex lVertex : mMesh.getVertices())
			if (!pChangedVerticesSet.contains(lVertex))
			{
				double xc = 0;
				double yc = 0;
				final ArrayList<Vertex> lNeighbours = lVertex.getNeighbours();
				boolean lNeighbourMarked = false;

				for (final Vertex lNeighbour : lNeighbours)
				{
					final double xn = lNeighbour.x;
					final double yn = lNeighbour.y;
					xc += xn;
					yc += yn;
					lNeighbourMarked |= pChangedVerticesSet.contains(lNeighbour);
				}

				if (lNeighbourMarked)
				{

					xc /= lNeighbours.size();
					yc /= lNeighbours.size();

					final double x = lVertex.x;
					final double y = lVertex.y;

					if (!(x == 0 || x == 1))
					{
						lVertex.x = pFactor * xc + (1 - pFactor) * x;
					}

					if (!(y == 0 || y == 1))
					{
						lVertex.y = pFactor * yc + (1 - pFactor) * y;
					}

					lNewFixedVerticesSet.add(lVertex);
				}
			}

		// System.out.println(Arrays.toString(mVerticesFlags));
		// System.out.println(lNewFixedVerticesSet.size());

		return lNewFixedVerticesSet;
	}

	public void relaxall(final double pFactor)
	{

		for (Vertex lVertex : mMesh.getVertices())
		{
			double xc = 0;
			double yc = 0;
			final ArrayList<Vertex> lNeighbours = lVertex.getNeighbours();

			for (final Vertex lNeighbour : lNeighbours)
			{
				final double xn = lNeighbour.x;
				final double yn = lNeighbour.y;
				xc += xn;
				yc += yn;
			}

			xc /= lNeighbours.size();
			yc /= lNeighbours.size();

			final double x = lVertex.x;
			final double y = lVertex.y;

			if (!(x == 0 || x == 1))
			{
				lVertex.x = pFactor * xc + (1 - pFactor) * x;
			}

			if (!(y == 0 || y == 1))
			{
				lVertex.y = pFactor * yc + (1 - pFactor) * y;
			}
		}

	}

	public void snap(	final int pLineSize,
										final double pRatio,
										double pThreshold)
	{
		final double[] imageline = new double[pLineSize];
		final double[] featureline = new double[pLineSize];

		mStressedEdges.clear();

		for (final Vertex lVertex : mMesh.getVertices())
		{
			final ArrayList<Vertex> lNeighbours = lVertex.getNeighbours();

			final double x = lVertex.x;
			final double y = lVertex.y;

			Vertex lBestFeatureVertexIndex = null;
			double lStrongestFeatureValueVertex = Double.NEGATIVE_INFINITY;

			double lStrongestFeatureF = -1;

			for (final Vertex lNeighbour : lNeighbours)
			{
				final double xn = lNeighbour.x;
				final double yn = lNeighbour.y;

				mDoubleImage.extractLine(	imageline,
																	2 * x - xn,
																	2 * y - yn,
																	xn,
																	yn);
				LinearFeatures.normalizeLine(imageline);
				LinearFeatures.computeFeatures(imageline, featureline);
				final int lStrongestFeatureIndexNeighbour = LinearFeatures.findStrongestRobustFeature(featureline,
																																															pThreshold);
				final double f = LinearFeatures.computeRobustFeatureF(featureline,
																															lStrongestFeatureIndexNeighbour);

				if (f > 0.5 && f < 0.75)
				{
					final double lStrongestFeatureValueNeighbour = featureline[lStrongestFeatureIndexNeighbour];

					if (lStrongestFeatureValueNeighbour > lStrongestFeatureValueVertex)
					{
						lBestFeatureVertexIndex = lNeighbour;
						lStrongestFeatureValueVertex = lStrongestFeatureValueNeighbour;
						lStrongestFeatureF = f;
					}
				}

			}

			if (lBestFeatureVertexIndex != null)
			{
				final double f = 2 * (lStrongestFeatureF - 0.5);

				if (f > 0)
				{
					final double vx = lBestFeatureVertexIndex.x - x;
					final double vy = lBestFeatureVertexIndex.y - y;

					mFeaturePoints.add(x, y, x + f * vx, y + f * vy);

					final double d = f * Math.sqrt((vx * vx + vy * vy));
					if (d > mMaximalAllowedStress)
					{
						final Edge lEdge = new Edge(lVertex,
																				lBestFeatureVertexIndex);
						lEdge.stress = d;
						mStressedEdges.add(lEdge);
					}

					if (!(x == 0 || x == 1))
					{
						lVertex.x = x + f * vx;
					}

					if (!(y == 0 || y == 1))
					{
						lVertex.y = y + f * vy;
					}

					mChangedVerticesSet.add(lVertex);
				}
			}

		}

	}

	public void snapaverage(final int pLineSize,
													final double pRatio,
													double pThreshold)
	{
		final double[] imageline = new double[pLineSize];
		final double[] featureline = new double[pLineSize];

		mStressedEdges.clear();

		for (final Vertex lVertex : mMesh.getVertices())
		{
			final ArrayList<Vertex> lNeighbours = lVertex.getNeighbours();

			final double x = lVertex.x;
			final double y = lVertex.y;

			for (final Vertex lNeighbour : lNeighbours)
			{
				final double xn = lNeighbour.x;
				final double yn = lNeighbour.y;

				mDoubleImage.extractLine(	imageline,
																	2 * x - xn,
																	2 * y - yn,
																	xn,
																	yn);

				LinearFeatures.computeFeatures(imageline, featureline);

				final int lStrongestFeatureIndex = LinearFeatures.strongestFeature(	featureline,
																																						pThreshold,
																																						mThresholdRatio,
																																						mThresholdSupport);

				final double ratio = 1.0 / lNeighbours.size();
				final double f = 2 * ((double) lStrongestFeatureIndex / (imageline.length - 1) - 0.5);

				if (f > 0 && f < 0.5)
				{
					final double vx = (lNeighbour.x - x);
					final double vy = (lNeighbour.y - y);

					mFeaturePoints.add(x, y, x + f * vx, y + f * vy);

					final double d = f * Math.sqrt((vx * vx + vy * vy));
					if (d > mMaximalAllowedStress)
					{
						final Edge lEdge = new Edge(lVertex, lNeighbour);
						lEdge.stress = d;
						mStressedEdges.add(lEdge);
					}

					if (!(x == 0 || x == 1))
					{
						lVertex.x = x + ratio * f * vx;
					}

					if (!(y == 0 || y == 1))
					{
						lVertex.y = y + ratio * f * vy;
					}

					mChangedVerticesSet.add(lVertex);
				}

			}
		}

	}

	public void delauney(final int pIterations)
	{
		for (int i = 0; i < pIterations; i++)
			for (Edge lEdge : mMesh.getEdges())
			{
				mMesh.delauneyFlipEdge(lEdge.u, lEdge.v);

				mChangedVerticesSet.add(lEdge.u);
				mChangedVerticesSet.add(lEdge.v);
			}
	}

	public void relaxbybreak()
	{
		for (Edge lEdge : mStressedEdges)
		{
			mMesh.breakEdgeInMiddle(lEdge.u, lEdge.v);
			mChangedVerticesSet.add(lEdge.u);
			mChangedVerticesSet.add(lEdge.v);
		}
	}

	public void decompose(final int pLineSize,
												final double pThreshold,
												int pMinimalEdgeSizeInPixels)
	{
		// mFeaturePoints.clear();
		final double[] imageline = new double[pLineSize];
		final double[] featureline = new double[pLineSize];

		for (Edge lEdge : mMesh.getEdges())
		{

			final boolean lEdgeRefined = refineEdge(imageline,
																							featureline,
																							lEdge.u,
																							lEdge.v,
																							pThreshold,
																							pMinimalEdgeSizeInPixels);

			if (lEdgeRefined)
			{
				mChangedVerticesSet.add(lEdge.u);
				mChangedVerticesSet.add(lEdge.v);
			}

		}

	}

	public final boolean refineEdge(final double[] imageline,
																	final double[] featureline,
																	final Vertex u,
																	final Vertex v,
																	double pThreshold,
																	int pMinimalEdgeSizeInPixels)
	{

		final double lDistance = Vertex.distance(u, v);
		if (lDistance > (mDoubleImage.getEpsilon() * pMinimalEdgeSizeInPixels))
		{

			final double Ux = 1.5 * u.x - 0.5 * v.x;
			final double Uy = 1.5 * u.y - 0.5 * v.y;
			final double Vx = 1.5 * v.x - 0.5 * u.x;
			final double Vy = 1.5 * v.y - 0.5 * u.y;

			mDoubleImage.extractLine(imageline, Ux, Uy, Vx, Vy);
			LinearFeatures.normalizeLine(imageline);
			LinearFeatures.computeRobustFeatures(imageline, featureline);
			final int lIndex = LinearFeatures.findStrongestRobustFeature(	featureline,
																																		pThreshold);
			final double f = LinearFeatures.computeRobustFeatureF(featureline,
																														lIndex);

			if (f >= 3.0 / 8 && f <= 5.0 / 8)
			{
				final double cutx = (1 - f) * Ux + f * Vx;
				final double cuty = (1 - f) * Uy + f * Vy;
				mMesh.breakEdge(u, v, cutx, cuty);
				return true;
			}
		}
		return false;
	}

	public final int countFeaturesOnTriangle(	double[] imageline,
																						double[] featureline,
																						final Triangle t,
																						final double pThreshold)
	{
		int count = 0;
		final Vertex u = t.u;
		final Vertex v = t.v;
		final Vertex w = t.w;

		final double iu = findStrongestFeature(	imageline,
																						featureline,
																						u,
																						v,
																						pThreshold);
		final double iv = findStrongestFeature(	imageline,
																						featureline,
																						v,
																						w,
																						pThreshold);
		final double iw = findStrongestFeature(	imageline,
																						featureline,
																						w,
																						u,
																						pThreshold);

		if (iu >= 0)
			count++;
		if (iv >= 0)
			count++;
		if (iw >= 0)
			count++;

		return count;
	}

	public final boolean getStrongestFeatureLocation(	double[] imageline,
																										double[] featureline,
																										final double[] location,
																										final Vertex u,
																										final Vertex v,
																										double pThreshold,
																										double min,
																										double max)
	{
		double f = findStrongestFeature(imageline,
																		featureline,
																		u,
																		v,
																		pThreshold);
		if (f >= min && f <= max)
		{
			final double lx = u.x + f * (v.x - u.x);
			final double ly = u.y + f * (v.y - u.y);
			location[0] = lx;
			location[1] = ly;
			return true;
		}
		else
			return false;

	}

	public final double findStrongestFeature(	double[] imageline,
																						double[] featureline,
																						final Vertex u,
																						final Vertex v,
																						double pThreshold)
	{

		mDoubleImage.extractLine(imageline, u.x, u.y, v.x, v.y);
		LinearFeatures.computeFeatures(imageline, featureline);
		final int lStrongestFeatureIndexNeighbour = LinearFeatures.strongestFeature(featureline,
																																								pThreshold,
																																								mThresholdRatio,
																																								mThresholdSupport);
		if (lStrongestFeatureIndexNeighbour >= 0)
		{
			final double f = ((double) lStrongestFeatureIndexNeighbour / (imageline.length - 1));
			return f;
		}
		else
			return -1;
	}

	public final DoubleBlockArrayList mFeaturePoints()
	{
		return mFeaturePoints;
	}

	public void clearFlags()
	{
		mChangedVerticesSet.clear();
	}

	/*
	 * public void setFlags(final boolean pFlag) {
	 * Arrays.fill(mFixedVerticesFlags, pFlag); }/*
	 */

	public void setOneRandomFlag(boolean pFlag)
	{
		mChangedVerticesSet.add(mMesh.getVertices()
																	.get(rnd.nextInt(mMesh.getNumberOfVertices())));
	}
}
