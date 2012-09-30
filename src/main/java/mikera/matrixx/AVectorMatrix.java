package mikera.matrixx;

import mikera.transformz.AffineMN;
import mikera.transformz.Transformz;
import mikera.vectorz.AVector;

/**
 * Abstract base class for matrices that use a collection of Vectors 
 * as storage for the matrix rows
 * 
 * @author Mike
 *
 */
public abstract class AVectorMatrix extends AMatrix {
	/**
	 * Gets a row of the matrix. Should be guaranteed to be an existing vector by all
	 * descendents of VectorMatrix.
	 */
	@Override
	public abstract AVector getRow(int row);

	@Override
	public double get(int row, int column) {
		return getRow(row).get(column);
	}

	@Override
	public void set(int row, int column, double value) {
		getRow(row).set(column,value);
	}
	
	public void transform(AVector source, AVector dest) {
		int rc=rowCount();
		for (int i=0; i<rc; i++) {
			dest.set(i,getRow(i).dotProduct(source));
		}
	}
	
	@Override
	public AVectorMatrix clone() {
		AVectorMatrix avm=(AVectorMatrix) super.clone();
		return avm;
	}
	

}
