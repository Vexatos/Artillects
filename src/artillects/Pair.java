package artillects;

public class Pair<L, R>
{

	private L left;
	private R right;

	public Pair(L left, R right)
	{
		this.left = left;
		this.right = right;
	}

	public Pair()
	{

	}

	public Pair<L, R> setLeft(L left)
	{
		this.left = left;
		return this;
	}

	public Pair<L, R> setRight(R right)
	{
		this.right = right;
		return this;
	}

	public L getLeft()
	{
		return left;
	}

	public R getRight()
	{
		return right;
	}

	public boolean bothContains(L left, R right)
	{
		return this.left == left && this.right == right;
	}

	public boolean leftContains(L left)
	{
		return this.left == left;
	}

	public boolean rightContains(R right)
	{
		return this.right == right;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Pair)
		{
			return ((Pair) o).right == this.right && ((Pair) o).left == this.left;
		}
		return false;
	}
}
