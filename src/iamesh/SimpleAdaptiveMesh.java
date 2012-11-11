package iamesh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.bridj.ann.Ptr;

import mesh.Edge;
import mesh.Mesh;
import mesh.Triangle;
import mesh.Vertex;
import utils.image.DoubleArrayImage;
import utils.image.linearfeatures.LinearFeatures;
import blocklists.DoubleBlockArrayList;

public class SimpleAdaptiveMesh
{
	private final DoubleArrayImage mDoubleImage;
	private final Mesh mAnalysingMesh, mAdaptiveMesh;

	final HashSet<Vertex> mChangedVerticesSet;

	DoubleBlockArrayList mFeaturePoints = new DoubleBlockArrayList(2);
	private final Random rnd = new Random();
	private double mThresholdRatio = 0.2;
	private double mThresholdSupport = 0.2;

	public SimpleAdaptiveMesh(DoubleArrayImage pDoubleImage,
														Mesh pAnalysingMesh,
														Mesh pAdaptiveMesh)
	{
		super();
		mDoubleImage = pDoubleImage;
		mAnalysingMesh = pAnalysingMesh;
		mAdaptiveMesh = pAdaptiveMesh;
		mChangedVerticesSet = new HashSet<Vertex>();
	}

	public void relaxall(final double pFactor)
	{

		for (Vertex lVertex : mAnalysingMesh.getVertices())
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

	public void delauney(final int pIterations)
	{
		for (int i = 0; i < pIterations; i++)
			for (Edge lEdge : mAnalysingMesh.getEdges())
			{
				mAnalysingMesh.delauneyFlipEdge(lEdge.u, lEdge.v);

				mChangedVerticesSet.add(lEdge.u);
				mChangedVerticesSet.add(lEdge.v);
			}
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

	public final boolean computeFeaturePoint(	final double[] imageline,
																						final double[] featureline,
																						final Vertex u,
																						final Vertex v,
																						double pThreshold,
																						int pMinimalEdgeSizeInPixels)
	{

		final double lDistance = Vertex.distance(u, v);
		if (lDistance > (mDoubleImage.getEpsilon() * pMinimalEdgeSizeInPixels))
		{

			final double Ux = 1.05 * u.x - 0.05 * v.x;
			final double Uy = 1.05 * u.y - 0.05 * v.y;
			final double Vx = 1.05 * v.x - 0.05 * u.x;
			final double Vy = 1.05 * v.y - 0.05 * u.y;

			mDoubleImage.extractLine(imageline, Ux, Uy, Vx, Vy);
			LinearFeatures.normalizeLine(imageline);
			LinearFeatures.computeFeatures(imageline, featureline);
			LinearFeatures.smoothLine(featureline);
			final int lIndex = LinearFeatures.findStrongestRobustFeature(	featureline,
																																		pThreshold);
			final double f = LinearFeatures.computeRobustFeatureF(featureline,
																														lIndex);

			if (f >= (0.05 / 1.1) && f <= (1.05 / 1.1))
			{
				final double cutx = (1 - f) * Ux + f * Vx;
				final double cuty = (1 - f) * Uy + f * Vy;
				mFeaturePoints.add(cutx, cuty);
				mAdaptiveMesh.addVertexDelauney(cutx, cuty);
			}
			else
			{
				final double cutx = 0.5 * u.x + 0.5 * v.x;
				final double cuty = 0.5 * u.y + 0.5 * v.y;
				mFeaturePoints.add(cutx, cuty);
				mAdaptiveMesh.addVertexDelauney(cutx, cuty);
			}
		}
		return false;
	}

	public final void computeFeaturePoints(	final int pLineSize,
																					double pThreshold,
																					int pMinimalEdgeSizeInPixels)
	{
		final double[] imageline = new double[pLineSize];
		final double[] featureline = new double[pLineSize];
		for (Edge lEdge : mAnalysingMesh.getEdges())
		{
			computeFeaturePoint(imageline,
													featureline,
													lEdge.u,
													lEdge.v,
													pThreshold,
													pMinimalEdgeSizeInPixels);
		}
	}

	public final DoubleBlockArrayList getFeaturePoints()
	{
		return mFeaturePoints;
	}

	public void clearFlags()
	{
		mChangedVerticesSet.clear();
	}

	public void setOneRandomFlag(boolean pFlag)
	{
		mChangedVerticesSet.add(mAnalysingMesh.getVertices()
																					.get(rnd.nextInt(mAnalysingMesh.getNumberOfVertices())));
	}
}
