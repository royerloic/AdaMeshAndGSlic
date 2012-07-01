package gslic;


import utils.graphics.hyperplane.Node;
import utils.graphics.hyperplane.primitives.Image;
import utils.graphics.hyperplane.primitives.LineMesh;
import utils.graphics.hyperplane.primitives.Point;
import utils.graphics.hyperplane.rendermodules.ImagesBasic;
import utils.graphics.hyperplane.rendermodules.PointsBasic;
import utils.graphics.hyperplane.rendermodules.RenderModuleInterface;
import utils.graphics.hyperplane.rendermodules.TriangleMeshVertexArrays;

import java.util.ArrayList;

import utils.image.DoubleArrayImage;
import utils.opengl.Color;

public class SLICVisualization
{
	private SLICAbstract mSLIC;

	private ArrayList<Point> mCentroids = new ArrayList<Point>();
	private LineMesh mLineMesh = new LineMesh();
	private Image mImage;
	
	private boolean mDisplayImage = true;

	private DoubleArrayImage mDoubleImage;
	private RenderModuleInterface<Image> mImageRenderModule;
	private RenderModuleInterface<LineMesh> mLineMeshRenderModule;
	private RenderModuleInterface mPointsRenderModule;



	public LineMesh getLineMesh()
	{
		return mLineMesh;
	}

	public Image getImage()
	{
		return mImage;
	}

	public SLICVisualization(SLICAbstract pSLIC)
	{
		super();
		mSLIC = pSLIC;
		mDoubleImage = mSLIC.getDoubleImage();
		mImage = new Image(	0,
												0,
												0.5,
												1,
												1,
												mDoubleImage.getGreyByteBuffer(0, 1),
												mDoubleImage.getWidth(),
												mDoubleImage.getHeight(),
												true);
		mImage.linear = false;

		final int lNumberOfCentroids = mSLIC.getNumberOfCentroids();
		for (int i = 0; i < lNumberOfCentroids; i++)
		{
			mCentroids.add(new Point(0, 0, 1, 0.0001, Color.red, 6));
		}
	}

	public void attachToNode(final Node pNode) throws InstantiationException,
																						IllegalAccessException
	{
		if (mDisplayImage )
		{
			mImageRenderModule = pNode.getRenderModule(ImagesBasic.class);
			mImageRenderModule.add(mImage);
		}

		mPointsRenderModule = pNode.getRenderModule(PointsBasic.class);
		for (Point lPoint : mCentroids)
		{
			mPointsRenderModule.add(lPoint);
		}

		mLineMeshRenderModule = pNode.getRenderModule(TriangleMeshVertexArrays.class);
		mLineMeshRenderModule.add(mLineMesh);
	}

	public void updateCentroids()
	{
		final int lNumberOfCentroids = mSLIC.getNumberOfCentroids();
		final double[] lCentroidX = mSLIC.getCentroidArrayX();
		final double[] lCentroidY = mSLIC.getCentroidArrayY();

		for (int i = 0; i < lNumberOfCentroids; i++)
		{
			final double x = mSLIC.convertToNormalizedImageCoordinatesX(lCentroidX[i]);
			final double y = mSLIC.convertToNormalizedImageCoordinatesY(lCentroidY[i]);
			//final double v = lCentroidV[i];
			final Point lPoint = mCentroids.get(i);
			lPoint.x = x;
			lPoint.y = y;
		}

	}

	public void updateBoudaries(final Color pBoundariesColors)
	{
		mLineMesh.clear();
		final int width = mDoubleImage.getWidth();
		final int height = mDoubleImage.getHeight();

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				final boolean lBottomSide = mSLIC.isBoundaryBottom(x, y);
				final boolean lTopSide = mSLIC.isBoundaryTop(x, y);
				final boolean lLeftSide = mSLIC.isBoundaryLeft(x, y);
				final boolean lRightSide = mSLIC.isBoundaryRight(x, y);

				addLines(	mLineMesh,
									mSLIC.convertToNormalizedImageCoordinatesX(x),
									mSLIC.convertToNormalizedImageCoordinatesY(y),
									lBottomSide,
									lTopSide,
									lLeftSide,
									lRightSide,
									pBoundariesColors);

			}

		mLineMeshRenderModule.update();

	}

	private final void addLines(final LineMesh pLineMesh,
															final double pX,
															final double pY,
															final boolean pBottomSide,
															final boolean pTopSide,
															final boolean pLeftSide,
															final boolean pRightSide,
															Color pBoundariesColors)
	{
		final double lPixelWidth = mDoubleImage.getPixelWidth();
		final double lPixelHeight = mDoubleImage.getPixelHeight();

		final double TLx = pX;
		final double TLy = pY;

		final double TRx = pX + lPixelWidth;
		final double TRy = pY;

		final double BLx = pX;
		final double BLy = pY + lPixelHeight;

		final double BRx = pX + lPixelWidth;
		final double BRy = pY + lPixelHeight;

		final int tli = pLineMesh.addVertexAndColor(TLx,
																								TLy,
																								1,
																								pBoundariesColors);
		final int tri = pLineMesh.addVertexAndColor(TRx,
																								TRy,
																								1,
																								pBoundariesColors);
		final int bli = pLineMesh.addVertexAndColor(BLx,
																								BLy,
																								1,
																								pBoundariesColors);
		final int bri = pLineMesh.addVertexAndColor(BRx,
																								BRy,
																								1,
																								pBoundariesColors);

		if (pBottomSide)
			pLineMesh.addLine(bli, bri);

		if (pRightSide)
			pLineMesh.addLine(bri, tri);

		// we only need to draw lines once!
		/*
		 * if (pTopSide) pLineMesh.addLine(tri, tli);
		 * 
		 * if (pLeftSide) pLineMesh.addLine(tli, bli); /*
		 */
	}

	public boolean isDisplayImage()
	{
		return mDisplayImage;
	}

	public void setDisplayImage(boolean displayImage)
	{
		mDisplayImage = displayImage;
	}

}
