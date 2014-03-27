package mikera.arrayz.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mikera.arrayz.Arrayz;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.matrixx.Matrixx;
import mikera.matrixx.impl.ZeroMatrix;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.impl.ZeroVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;

/**
 * Class representing an immutable array filled entirely with zeros.
 * 
 * @author Mike
 *
 */
public final class ZeroArray extends AbstractArray<INDArray> implements ISparse {
	private static final long serialVersionUID = 7355257027343666183L;

	private final int[] shape; 
	
	private ZeroArray(int[] shape)  {
		this.shape=shape;
	}
	
	public static ZeroArray wrap(int... shape) {
		return new ZeroArray(shape);
	}
	
	public static ZeroArray create(int... shape) {
		return new ZeroArray(shape.clone());
	}

	@Override
	public int dimensionality() {
		return shape.length;
	}
	
	@Override
	public long nonZeroCount() {
		return 0;
	}

	@Override
	public int[] getShape() {
		return shape;
	}

	@Override
	public double get(int... indexes) {
		if (!IntArrays.validIndex(indexes,shape)) throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex(this, indexes));
		return 0.0;
	}

	@Override
	public double get() {
		if (shape.length!=0) throw new IllegalArgumentException(ErrorMessages.invalidIndex(this));
		return 0.0;
	}

	@Override
	public double get(int x) {
		if (shape.length!=1) throw new IllegalArgumentException(ErrorMessages.invalidIndex(this));
		if ((x<0)||(x>=shape[0])) throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex(this, x));
		return 0.0;
	}

	@Override
	public double get(int x, int y) {
		if (shape.length!=2) throw new IllegalArgumentException(ErrorMessages.invalidIndex(this));
		if ((x<0)||(x>=shape[0])) throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex(this, x,y));
		if ((y<0)||(y>=shape[1])) throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex(this, x,y));
		return 0.0;
	}

	@Override
	public void set(int[] indexes, double value) {
		throw new UnsupportedOperationException(ErrorMessages.immutable(this));
	}

	@Override
	public INDArray slice(int majorSlice) {
		if ((majorSlice<0)||(majorSlice>=shape[0])) throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, majorSlice));
		switch (dimensionality()) {
		case 1: return ImmutableScalar.ZERO;
		case 2: return ZeroVector.create(shape[1]);
		case 3: return ZeroMatrix.create(shape[1],shape[2]);
		default: return ZeroArray.wrap(IntArrays.removeIndex(shape, 0));
		}
	}

	@Override
	public INDArray slice(int dimension, int index) {
		if (dimension==0) return slice(index);
		return Arrayz.createZeroArray(IntArrays.removeIndex(shape, dimension));
	}
	
	@Override
	public List<INDArray> getSlices() {
		int sc=sliceCount();
		if (sc==0) return Collections.EMPTY_LIST;
		ArrayList<INDArray> al=new ArrayList<INDArray>(sc);
		INDArray z=slice(0);
		for (int i=0; i<sc; i++) {
			al.add(z);
		}
		return al;
	}

	@Override
	public int sliceCount() {
		return shape[0];
	}

	@Override
	public long elementCount() {
		return IntArrays.arrayProduct(shape);
	}

	@Override
	public boolean isView() {
		return false;
	}
	
	@Override
	public boolean isMutable() {
		return false;
	}
	
	@Override
	public boolean isZero() {
		return true;
	}
	
	@Override
	public ZeroArray getTranspose() {
		return ZeroArray.wrap(IntArrays.reverse(shape));
	}
	
	@Override
	public boolean equalsArray(double[] data, int offset) {
		return DoubleArrays.isZero(data, offset, (int)elementCount());
	}
	
	@Override
	public INDArray clone() {
		return Arrayz.newArray(shape);
	}
	
	@Override
	public INDArray sparseClone() {
		switch (dimensionality()) {
			case 0: return ImmutableScalar.ZERO;
			case 1: return Vectorz.createSparseMutable(shape[0]);
			case 2: return Matrixx.createSparseRows(this);
			default: {
				ArrayList<INDArray> al=new ArrayList<INDArray>();
				int n=sliceCount();
				for (int i=0; i<n; i++) {
					al.add(slice(i).sparseClone());
				}
				return SliceArray.create(al);
			}
		}
	}
	
	@Override
	public ZeroArray exactClone() {
		return create(shape);
	}

}
