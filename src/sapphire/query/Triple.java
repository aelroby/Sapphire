package sapphire.query;

import java.util.*;
import java.util.stream.Collectors;

public class Triple {
	public String subject;
	public String predicate;
	public String object;
	public String fromSeed;
	public Triple previousTriple;
	public Integer cost;
	public HashMap<String, Integer> connectedSeedsAndWeights = new HashMap<>();

	public Triple(String subject, String predicate, String object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.previousTriple = null;
		this.cost = 0;
	}

	public Triple(String subject, String predicate, String object, String seedName) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.fromSeed = seedName;
		//initialize literalsToConnect
		this.connectedSeedsAndWeights.put(seedName, 0);
		this.cost = 0;
		this.previousTriple = null;
	}

	public Triple(String subject, String predicate, String object, Integer cost, String seedName) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.cost = cost;
		this.fromSeed = seedName;
		this.previousTriple = null;
	}

	public Triple(Triple tp, String predicate, String object, Integer cost) {
		this.subject = tp.object;
		this.predicate = predicate;
		this.object = object;
		this.cost = cost;
		this.fromSeed = tp.fromSeed;
		this.previousTriple = tp;
		this.addConnectedSeeds();
	}

	
	public void addConnectedSeeds(){
		for(Map.Entry<String, Integer> me: this.previousTriple.getConnectedSeedsAndWeights().entrySet()){
			if(!this.connectedSeedsAndWeights.containsKey(me.getKey())){
				this.connectedSeedsAndWeights.put(me.getKey(), me.getValue() + this.cost);
			}else{
				int oldValue = this.connectedSeedsAndWeights.get(me.getKey());
				this.connectedSeedsAndWeights.replace(me.getKey(), oldValue + this.cost);
			}
		}
	}

	public void setFromSeed(String seedName){
		this.fromSeed = seedName;
	}

	
	public void setPreviousTriple(Triple previous){
		this.previousTriple = previous;
	}

	//when two triples are connected, recalculate their distance to fromseed
	public void calculateSeedsAndWeights(Triple tp){

		//adding seedNames, and update distance
		for(Map.Entry<String, Integer> me: tp.getConnectedSeedsAndWeights().entrySet()){
			if(!this.connectedSeedsAndWeights.containsKey(me.getKey())){
				this.connectedSeedsAndWeights.put(me.getKey(), me.getValue());
			}else{
				if(this.connectedSeedsAndWeights.get(me.getKey()) > me.getValue()){
					this.connectedSeedsAndWeights.replace(me.getKey(), me.getValue());
				}
			}
		}

		for(Map.Entry<String, Integer> me: this.getConnectedSeedsAndWeights().entrySet()){
			if(!tp.getConnectedSeedsAndWeights().containsKey(me.getKey())){
				tp.getConnectedSeedsAndWeights().put(me.getKey(), me.getValue());
			}else{
				if(tp.getConnectedSeedsAndWeights().get(me.getKey()) > me.getValue()){
					tp.getConnectedSeedsAndWeights().replace(me.getKey(), me.getValue());
				}
			}
		}

	}

	public void printWeightsToAllSeeds(){
		for(Map.Entry<String, Integer> me: this.connectedSeedsAndWeights.entrySet()){
			System.out.println("|| To Seed: " + me.getKey() + " Weights: " + me.getValue());
		}
	}

	public HashMap<String, Integer> getClosestAndSecondClosestTerminals(){
		HashMap<String, Integer> hm = new HashMap<>();
		String[] seedName = new String[2];
		int smallestWeight = 999;
		int secondSmallestWeight = 999;

		if(this.connectedSeedsAndWeights.size() < 2){
			hm.put(this.fromSeed, this.connectedSeedsAndWeights.get(this.fromSeed));
			return hm;
		}

		for(Map.Entry<String, Integer> me: this.connectedSeedsAndWeights.entrySet()){
			if(me.getValue() < smallestWeight){
				secondSmallestWeight = smallestWeight;
				seedName[1] = seedName[0];

				smallestWeight = me.getValue();
				seedName[0] = me.getKey();

			}else{
				if(me.getValue() < secondSmallestWeight){
					secondSmallestWeight = me.getValue();
					seedName[1] = me.getKey();
				}
			}
		}

		hm.put(seedName[0], smallestWeight);
		hm.put(seedName[1], secondSmallestWeight);

		return hm;
	}

	public HashMap<String, Integer> getClosestAndSecondClosestTerminals2(){
		HashMap<String, Integer> hm = new HashMap<>();

		if(this.connectedSeedsAndWeights.size() < 2){
			hm.put(this.fromSeed, this.connectedSeedsAndWeights.get(this.fromSeed));
			return hm;
		}
		HashMap<String, Integer> temp = this.connectedSeedsAndWeights.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
				(e1, e2) -> e1, HashMap::new));
		for (Map.Entry<String, Integer> entry : temp.entrySet()) {
			hm.put(entry.getKey(), entry.getValue());
			if (hm.size() == 2) {
				break;
			}
		}
		return hm;
	}

	public int getAccumulatedCost(){
		if(this.connectedSeedsAndWeights.isEmpty()){
			return 0;
		}else{
			return this.connectedSeedsAndWeights.get(this.fromSeed);
		}
	}

	public HashMap<String, Integer> getConnectedSeedsAndWeights(){
		return this.connectedSeedsAndWeights;
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
		return "( " + this.subject + " " + this.predicate + " " + this.object + " ." + " || FromSeed: " + this.fromSeed + " [[" + getAccumulatedCost() + "]]) --- |Previous Triple|---> " + this.previousTriple;
	}

}
