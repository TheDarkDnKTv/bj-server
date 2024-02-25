package thedarkdnktv.openbjs.api.network.base;

public abstract class LazyLoadBase<T> {
	private T value;
	private boolean isLoaded = false;
	
	public T getValue() {
		if (!isLoaded) {
			this.value = this.load();
			this.isLoaded = true;
		}
		
		return this.value;
	}
	
	protected abstract T load();
}
