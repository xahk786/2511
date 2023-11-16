package dungeonmania.MovingEntities;

public interface SubjectPlayer {

    public void attach(ObserverEnemy o);
    public void dettach(ObserverEnemy o);
    public void notifyObserver();
}
