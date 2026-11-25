package royaume.service;

import org.springframework.stereotype.Service;

import royaume.dao.IDAOAttaque;
import royaume.dao.IDAOPartie;

@Service 
public class TickerService {

    private final IDAOPartie daoPartie;

    private final IDAOAttaque daoAttaque;

    public TickerService(IDAOPartie daoPartie, IDAOAttaque daoAttaque) {
        this.daoPartie = daoPartie;
        this.daoAttaque = daoAttaque;
    }



}
