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

	public boolean isInside(double pX, double pY)
	{
		final double Ux = v.x - u.x;
		final double Uy = v.y - u.y;

		final double Vx = w.x - u.x;
		final double Vy = w.y - u.y;

		final double Xx = pX - u.x;
		final double Xy = pY - u.y;
				
		final double dot00 = dot(Ux,Uy,Ux,Uy);
		final double dot01 = dot(Ux,Uy,Vx,Vy);
		final double dot02 = dot(Ux,Uy,Xx,Xy);
		final double dot11 = dot(Vx,Vy,Vx,Vy);
		final double dot12 = dot(Vx,Vy,Xx,Xy);

		// Compute barycentric coordinates
		final double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
		final double x = (dot11 * dot02 - dot01 * dot12) * invDenom;
		final double y = (dot00 * dot12 - dot01 * dot02) * invDenom;

		return x >= 0 && y >= 0 && x + y <= 1;
	}

	private static final double dot(double pUx, double pUy, double pVx, double pVy)
	{
		return pUx*pVx+pUy*pVy;
	}

	
}
