package sapphire.query;

public class Triple {
	public String subject;
	public String predicate;
	public String object;
	
	public Triple(String subject, String predicate, String object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		
		Triple that = (Triple) o;
		if(this.subject.equals(that.subject) && this.predicate.equals(that.predicate) && this.object.equals(that.object)) {
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		String all = subject + predicate + object;
		return all.hashCode();
	}
	
	@Override
	public String toString() {
		return this.subject + " " + this.predicate + " " + this.object + " .";
	}

}
