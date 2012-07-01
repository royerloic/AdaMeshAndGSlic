package superpixel;

import gslic.LabelField;

public class LabelFieldToSuperPixels
{

	public static void convert(	LabelField pLabeledField,
															SuperPixels pSuperPixels)
	{
		final int width = pLabeledField.width;
		final int height = pLabeledField.height;
		final int length = width*height;
		final int[] array = pLabeledField.array; 
		
		final SuperPixel[] lSuperPixelArray = new SuperPixel[length];
		
		for(int i=0; i<length; i++)
		{
			final int lLabel = array[i];
			
			//TODO: fix this, I commented the line below, what was I doing here?
			//final int up = i-width
			//TODO: finish!
		}
	
		
	}

	private final int recursiveComponentWalking(final int[] pLabelField,
																							final int pWidth,
																							final int pHeight,
																							final boolean[] pVisited,
																							final int pLabel,
																							final int pX,
																							final int pY)
	{
		if (clampX(pWidth, pX) != pX || clampY(pHeight, pY) != pY)
			return 0;

		final int lOffset = pX + pWidth * pY;

		if (!pVisited[lOffset] && pLabelField[lOffset] == pLabel)
		{
			pVisited[lOffset] = true;

			final int c1 = recursiveComponentWalking(	pLabelField,
																								pWidth,
																								pHeight,
																								pVisited,
																								pLabel,
																								pX,
																								pY - 1);
			final int c2 = recursiveComponentWalking(	pLabelField,
																								pWidth,
																								pHeight,
																								pVisited,
																								pLabel,
																								pX - 1,
																								pY);
			final int c3 = recursiveComponentWalking(	pLabelField,
																								pWidth,
																								pHeight,
																								pVisited,
																								pLabel,
																								pX + 1,
																								pY);
			final int c4 = recursiveComponentWalking(	pLabelField,
																								pWidth,
																								pHeight,
																								pVisited,
																								pLabel,
																								pX,
																								pY + 1);

			return 1 + c1 + c2 + c3 + c4;
		}
		else
		{
			return 0;
		}
	}

	private final int clampX(final int pWidth, final int pX)
	{
		return pX < 0 ? 0 : (pX >= pWidth ? pWidth - 1 : pX);
	}

	private final int clampY(final int pHeight, final int pY)
	{
		return pY < 0 ? 0 : (pY >= pHeight ? pHeight - 1 : pY);
	}

}
