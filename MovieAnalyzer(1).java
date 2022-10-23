import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MovieAnalyzer {

  class Movie {

    String seriesTitle;
    int releasedYear;
    String certificate;
    int runtime;
    Set<String> genres;
    float imdbRating;
    String overview;
    String metaScore;
    String director;
    List<String> stars;
    int noOfVotes;
    int gross;

    public Movie(String seriesTitle, int releasedYear, String certificate, int runtime,
        Set<String> genres, float imdbRating, String overview, String metaScore, String director,
        List<String> stars, int noOfVotes, int gross) {
      this.seriesTitle = seriesTitle;
      this.releasedYear = releasedYear;
      this.certificate = certificate;
      this.runtime = runtime;
      this.genres = genres;
      this.imdbRating = imdbRating;
      this.overview = overview;
      this.metaScore = metaScore;
      this.director = director;
      this.stars = stars;
      this.noOfVotes = noOfVotes;
      this.gross = gross;
    }

    public String getSeriesTitle() {
      return seriesTitle;
    }

    public int getReleasedYear() {
      return releasedYear;
    }

    public String getCertificate() {
      return certificate;
    }

    public int getRuntime() {
      return runtime;
    }

    public Set<String> getGenres() {
      return genres;
    }

    public float getImdbRating() {
      return imdbRating;
    }

    public List<StarRating> getStarRating() {
      List<StarRating> starRatings = new ArrayList<>();
      for (String star : stars
      ) {
        starRatings.add(new StarRating(star, imdbRating));
      }
      return starRatings;
    }

    public String getOverview() {
      return overview;
    }

    public String getMetaScore() {
      return metaScore;
    }

    public String getDirector() {
      return director;
    }

    public List<String> getStars() {
      return stars;
    }

    public List<List<String>> getCoStars() {
      List<List<String>> coStars = new ArrayList<>();
      for (int i = 0; i < stars.size(); i++) {
        for (int j = i + 1; j < stars.size(); j++) {
          List<String> part = new ArrayList<>();
          part.add(stars.get(i));
          part.add(stars.get(j));
          coStars.add(part);
        }
      }
      return coStars;
    }

    public int getNoOfVotes() {
      return noOfVotes;
    }

    public int getGross() {
      return gross;
    }

    public List<StarGross> getStarGross() {
      List<StarGross> starGrosses = new ArrayList<>();
      if (gross>0) {
        for (String star : stars
        ) {
          starGrosses.add(new StarGross(star, gross));
        }
      }
      return starGrosses;
    }

  }

  class StarRating {

    String star;
    float rating;

    public StarRating(String star, float rating) {
      this.star = star;
      this.rating = rating;
    }

    public String getStar() {
      return star;
    }

    public float getRating() {
      return rating;
    }
  }

  class StarGross {

    String star;
    int gross;

    public StarGross(String star, int gross) {
      this.star = star;
      this.gross = gross;
    }

    public String getStar() {
      return star;
    }

    public int getGross() {
      return gross;
    }
  }

  List<Movie> movies;

  public MovieAnalyzer(String datasetPath) {
    movies = new ArrayList<>();
    try {
      Scanner scanner = new Scanner(new FileReader(datasetPath, StandardCharsets.UTF_8));
      scanner.nextLine();
      String line;
      Pattern pattern = Pattern.compile("(,)?((\"[^\"]*(\"{2})*[^\"]*\")*[^,]*)");
      while (scanner.hasNextLine()) {
        line = scanner.nextLine();
        Matcher matcher = pattern.matcher(line);
        List<String> data = new ArrayList<>();
        while (matcher.find()) {
          String cell = matcher.group(2);
          if (cell.length() > 0 && cell.charAt(0) == '"') {
            data.add(cell.substring(1, cell.length() - 1));
          } else {
            data.add(cell);
          }
        }
        String seriesTitle = data.get(1);
        int releasedYear = Integer.parseInt(data.get(2));
        String certificate = data.get(3);
        int runtime = Integer.parseInt(data.get(4).split(" ")[0]);
        Set<String> genres = new HashSet<>(
            Arrays.asList(data.get(5).split(", ")));
        float imdbRating = Float.parseFloat(data.get(6));
        String overview = data.get(7);
        String metaScore = data.get(8);
        String director = data.get(9);
        List<String> stars = Arrays.asList(data.get(10), data.get(11), data.get(12), data.get(13));
        stars.sort(Comparator.naturalOrder());
        int noOfVotes = Integer.parseInt(data.get(14));
        int gross = 0;
        if (data.get(15).length() != 0) {
          gross =  Integer.parseInt(data.get(15).replace(",", ""));
        }
        Movie movie = new Movie(seriesTitle, releasedYear, certificate, runtime, genres, imdbRating,
            overview, metaScore, director, stars, noOfVotes, gross);
        movies.add(movie);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Map<Integer, Integer> getMovieCountByYear() {
    return movies.stream()
        .sorted(Comparator.comparing(Movie::getReleasedYear).reversed()).collect(
            Collectors.groupingBy(Movie::getReleasedYear, LinkedHashMap::new,
                Collectors.summingInt(p -> 1)));
  }

  public Map<String, Integer> getMovieCountByGenre() {
    return movies.stream().map(Movie::getGenres).flatMap(Collection::stream)
        .collect(Collectors.groupingBy(p -> p, Collectors.summingInt(p -> 1))).entrySet().stream()
        .sorted(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed()
            .thenComparing(Entry::getKey)).collect(
            Collectors.toMap(Map.Entry<String, Integer>::getKey,
                Map.Entry<String, Integer>::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

  }

  public Map<List<String>, Integer> getCoStarCount() {
    return movies.stream().map(Movie::getCoStars).flatMap(Collection::stream)
        .collect(Collectors.groupingBy(e -> e, Collectors.summingInt(p -> 1))).entrySet().stream()
        .sorted(Comparator.comparing(Map.Entry<List<String>, Integer>::getValue).reversed()
            .thenComparing(e -> e.getKey().get(0))).collect(
            Collectors.toMap(Map.Entry<List<String>, Integer>::getKey,
                Map.Entry<List<String>, Integer>::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
  }

  public List<String> getTopMovies(int top_k, String by) {
    if (by.equals("runtime")) {
      return movies.stream().sorted(
              Comparator.comparing(Movie::getRuntime).reversed().thenComparing(Movie::getSeriesTitle))
          .limit(top_k).map(Movie::getSeriesTitle).collect(Collectors.toList());
    } else {
      return movies.stream().sorted(
              Comparator.comparing((Movie e) -> e.getOverview().length()).reversed()
                  .thenComparing(Movie::getSeriesTitle)).limit(top_k).map(Movie::getSeriesTitle)
          .collect(Collectors.toList());
    }
  }

  public List<String> getTopStars(int top_k, String by) {
    if (by.equals("rating")) {
      return movies.stream().map(Movie::getStarRating).flatMap(Collection::stream)
          .collect(Collectors.groupingBy(StarRating::getStar, Collectors.averagingDouble(
              StarRating::getRating)))
          .entrySet().stream()
          .sorted(Comparator.comparing(Map.Entry<String, Double>::getValue).reversed()
              .thenComparing(Entry::getKey)).map(Entry::getKey).limit(top_k)
          .collect(Collectors.toList());
    } else {
      return movies.stream().map(Movie::getStarGross).flatMap(Collection::stream)
          .collect(Collectors.groupingBy(StarGross::getStar, Collectors.averagingDouble(
              StarGross::getGross)))
          .entrySet().stream()
          .sorted(Comparator.comparing(Map.Entry<String, Double>::getValue).reversed()
              .thenComparing(Entry::getKey)).map(Entry::getKey).limit(top_k)
          .collect(Collectors.toList());
    }
  }

  public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
    return movies.stream().filter(p -> p.getGenres().contains(genre))
        .filter(p -> p.getImdbRating() >= min_rating)
        .filter(p -> p.getRuntime() <= max_runtime).map(Movie::getSeriesTitle).sorted()
        .collect(Collectors.toList());
  }
}
