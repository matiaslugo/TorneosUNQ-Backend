package application.controller;

import application.domain.Championship;
import application.domain.*;
import application.dto.ChampionshipDTO;
import application.repository.ChampionshipRepository;
import application.repository.TeamRepository;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Transactional
@RestController
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST})
public class ChampionshipController {

    @Autowired
    private ChampionshipRepository repository;

    @Autowired
    private TeamRepository repositoryTeam;

    @GetMapping("/championships")
    public Collection<ChampionshipDTO> getAll() {
        //QUITAR EL COMENTARIO PARA LA PRIMERA VEZ DE LA EJECUCION
        //Team qac = repositoryTeam.findById((long) 1).get();

        /*Team qac = repositoryTeam.findById((long) 1).get();

        Team boca = repositoryTeam.findById((long) 7).get();

        StatisticTeam st1 = new StatisticTeam(qac,1,3,1,0,0,3,2,1);
        StatisticTeam st2 = new StatisticTeam(boca,1,0,0,0,1,2,3,-1);

        List<StatisticTeam> statisticTeams = new ArrayList<StatisticTeam>();
        statisticTeams.add(st1);
        statisticTeams.add(st2);

        Positions posit = new Positions(statisticTeams,"Torneo");
*/
        List<Positions> positions = new ArrayList<Positions>();

        //positions.add(posit);

        //Championship ch = new Championship("TORNEO UNQ","ALTO TORNEO",new DateTime("1977-10-06"),new DateTime("1977-12-06"),positions,new Fixture());

        //repository.save(ch);
        Collection<Championship> championships;
        ArrayList<ChampionshipDTO> championshipsReturn = new ArrayList<ChampionshipDTO>();
        championships = (Collection<Championship>) repository.findAll().stream()
        .collect(Collectors.toList());
        
            for (Championship ch : championships) {
              ChampionshipDTO championship = mapChampionship(ch);
              championshipsReturn.add(championship);
            }

        return championshipsReturn;
    }

    public ChampionshipDTO mapChampionship(Championship championshipToMap)
    {
        ChampionshipDTO newChampionship = new ChampionshipDTO();
        newChampionship.setId((championshipToMap.getId().toString()));
        newChampionship.setName(championshipToMap.getName());
        newChampionship.setDescription(championshipToMap.getDescription());
        newChampionship.setStartDate(championshipToMap.getStartDate().toString().split("T")[0]);
        newChampionship.setFinishDate(championshipToMap.getFinishDate().toString().split("T")[0]);

        return newChampionship;
    }

    @PostMapping(path="/championshipCreate/")
    // @RequestMapping(value="/championshipCreate",method= {RequestMethod.GET,RequestMethod.POST})
    public void championshipCreate(@RequestBody ChampionshipDTO championship) {

        Championship newChampionship = new Championship();

        newChampionship.setName(championship.getName());
        newChampionship.setDescription(championship.getDescription());
        newChampionship.setFixture(new Fixture());
        newChampionship.setStartDate(new DateTime(championship.getStartDate()));
        newChampionship.setFinishDate(new DateTime(championship.getFinishDate()));
        newChampionship.setPositions(new ArrayList<Positions>());

         repository.save(newChampionship);
    }

     @GetMapping("/championshipById/{id}")
    public ChampionshipDTO championshipById(@PathVariable String id) {
        Optional<Championship> championship = repository.findById(Long.parseLong(id));
        ChampionshipDTO championshipDTO = new ChampionshipDTO();
        championshipDTO.setName(championship.get().getName());
        championshipDTO.setDescription(championship.get().getDescription());
        String[] startDate = championship.get().getStartDate().toString().split("T");
        championshipDTO.setStartDate(startDate[0]);
        String[] finishDate = championship.get().getFinishDate().toString().split("T");
        championshipDTO.setFinishDate(finishDate[0]);
        return championshipDTO;
    }

    @PostMapping(path ="/championshipUpdate/{id}")
    public void championshipUpdate(@PathVariable String id, @RequestBody ChampionshipDTO championship) {

        Optional<Championship> championshipById = repository.findById(Long.parseLong(id));
        championshipById.get().setName(championship.getName());
        championshipById.get().setDescription(championship.getDescription());
        championshipById.get().setStartDate(new DateTime(championship.getStartDate()));
        championshipById.get().setFinishDate(new DateTime(championship.getFinishDate()));
        repository.save(championshipById.get());
    }

    @RequestMapping(method=RequestMethod.DELETE, path="/deleteChampionship/{id}")
    public void deleteChampionship(@PathVariable String id) {
        repository.deleteById(Long.parseLong(id));
    }

    @GetMapping("/tablePositions")
    public Collection<StatisticTeam> getStatisticTeam() {

        try {
            return (Collection<StatisticTeam>) repository.findAll().get(0).getPositions().get(0).getStatisticTeams().stream()
                    .collect(Collectors.toList());

        }
        catch(Exception e){
            return new ArrayList<StatisticTeam>();
        }

    }

    @PostMapping(path ="/addTeams/")
    public void addTeams(@RequestBody ArrayList<String> teamsId) {
        ArrayList<Team> addTeamsChampionship = new ArrayList<Team>();
        Championship championship = repository.findLastChampionship();

        for (String teamId : teamsId) {
           Team team = repositoryTeam.findById(Long.parseLong(teamId)).get();
           if (championship.allTeams().contains(team))
           {
            championship.allTeams().remove(team);
           }
            championship.allTeams().add(team);
        }

        repository.save(championship);
    }

    /* @GetMapping("/matches")
    public Collection<Game> getGames() {

        Fixture currentFixture = repository.findAll().get(0).getFixture();
 
        Collection<Game> matches = (Collection<Game>) currentFixture.getGame().stream()
                .collect(Collectors.toList());

        return matches;
    } */

    /* @GetMapping("/fixture")
    public int getFixture() {

        Fixture currentFixture = repository.findAll().get(0).getFixture();

        return currentFixture.getCurrentMatchWeek();
    } */

    @GetMapping("/fixtureGenerate")
    public void fixtureGenerate() {

        String idCurrentChampionship = repository.findLastChampionship().getId().toString();
        ArrayList<Team> teams = repositoryTeam.findTeamsByChampionshipId(idCurrentChampionship);
        FixtureGenerator fixtureGenerate = new FixtureGenerator();
        Fixture fixtureNew = fixtureGenerate.FixtureCreate(teams);
        Championship currentChampionship = repository.findLastChampionship();
        currentChampionship.setFixture(fixtureNew);
        repository.save(currentChampionship);
        
    } 

    @GetMapping("/fixture")
    public Collection<Game> fixture() {

        Championship currentChampionship = repository.findLastChampionship();
        Collection<Game> games = (Collection<Game>) currentChampionship.getFixture().getGame();
        if(games.size() == 0)
        {
            games = new ArrayList<Game>();
        }

        return games;
        
    }

}
