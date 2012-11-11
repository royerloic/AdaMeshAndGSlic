package mesh;

public class Triangle
{
	public final Vertex u;
	public final Vertex v;
	public final Vertex w;

	public Triangle(final Vertex pU, final Vertex pV, final Vertex pW)
	{
		super();
		u = pU;
		v = pV;
		w = pW;

		u.belongsToTriangle(this);
		v.belongsToTriangle(this);
		w.belongsToTriangle(this);
	}

	public void unlink()
	{
		u.unlinkFrom(this);
		v.unlinkFrom(this);
		w.unlinkFrom(this);
	}

	@Override
	public String toString()
	{
		return "Triangle [u=" + u + ", v=" + v + ", w=" + w + "]";
	}

	private double signHalfPlane(	double p1x,
																double p1y,
																double p2x,
																double p2y,
																double p3x,
																double p3y)
	{
		return (p1x - p3x) * (p2y - p3y) - (p2x - p3x) * (p1y - p3y);
	}

	public boolean isStrictlyInside(double pX, double pY)
	{
		boolean b1, b2, b3;

		b1 = signHalfPlane(pX, pY, u.x, u.y, v.x, v.y) < 0.0f;
		b2 = signHalfPlane(pX, pY, v.x, v.y, w.x, w.y) < 0.0f;
		b3 = signHalfPlane(pX, pY, w.x, w.y, u.x, u.y) < 0.0f;

		return ((b1 == b2) && (b2 == b3));
	}

	public boolean isInside(double pX, double pY)
	{
		boolean b1, b2, b3;

		b1 = signHalfPlane(pX, pY, u.x, u.y, v.x, v.y) <= 0.0f;
		b2 = signHalfPlane(pX, pY, v.x, v.y, w.x, w.y) <= 0.0f;
		b3 = signHalfPlane(pX, pY, w.x, w.y, u.x, u.y) <= 0.0f;

		return ((b1 == b2) && (b2 == b3));
	}

	public boolean isInsideOld(double pX, double pY)
	{
		final double Ux = v.x - u.x;
		final double Uy = v.y - u.y;

		final double Vx = w.x - u.x;
		final double Vy = w.y - u.y;

		final double Xx = pX - u.x;
		final double Xy = pY - u.y;

		final double dot00 = dot(Ux, Uy, Ux, Uy);
		final double dot01 = dot(Ux, Uy, Vx, Vy);
		final double dot02 = dot(Ux, Uy, Xx, Xy);
		final double dot11 = dot(Vx, Vy, Vx, Vy);
		final double dot12 = dot(Vx, Vy, Xx, Xy);

		// Compute barycentric coordinates
		final double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
		final double x = (dot11 * dot02 - dot01 * dot12) * invDenom;
		final double y = (dot00 * dot12 - dot01 * dot02) * invDenom;

		return x >= 0 && y >= 0 && x + y <= 1;
	}

	public boolean isStrictlyInsideOld(double pX, double pY)
	{
		final double Ux = v.x - u.x;
		final double Uy = v.y - u.y;

		final double Vx = w.x - u.x;
		final double Vy = w.y - u.y;

		final double Xx = pX - u.x;
		final double Xy = pY - u.y;

		final double dot00 = dot(Ux, Uy, Ux, Uy);
		final double dot01 = dot(Ux, Uy, Vx, Vy);
		final double dot02 = dot(Ux, Uy, Xx, Xy);
		final double dot11 = dot(Vx, Vy, Vx, Vy);
		final double dot12 = dot(Vx, Vy, Xx, Xy);

		// Compute barycentric coordinates
		final double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
		final double x = (dot11 * dot02 - dot01 * dot12) * invDenom;
		final double y = (dot00 * dot12 - dot01 * dot02) * invDenom;

		return x > 0 && y > 0 && x + y < 1;
	}

	private static final double dot(double pUx,
																	double pUy,
																	double pVx,
																	double pVy)
	{
		return pUx * pVx + pUy * pVy;
	}

	public boolean isVertex(double pX, double pY)
	{
		return u.is(pX, pY) || v.is(pX, pY) || w.is(pX, pY);
	}

}
