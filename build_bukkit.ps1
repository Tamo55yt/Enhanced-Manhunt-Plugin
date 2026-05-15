Write-Host "Manhunt BUKKIT Build Scripti basliyor..." -ForegroundColor Yellow

$profiles = @(
    @{ id = "v26_1"; tag = "26.x" },
    @{ id = "v1_21"; tag = "1.21.x" },
    @{ id = "v1_20"; tag = "1.20.x" },
    @{ id = "v1_19"; tag = "1.19.x" },
    @{ id = "v1_18"; tag = "1.18.x" },
    @{ id = "v1_17"; tag = "1.17.x" },
    @{ id = "v1_16"; tag = "1.16.x" },
    @{ id = "v1_15"; tag = "1.15.x" },
    @{ id = "v1_14"; tag = "1.14.x" },
    @{ id = "v1_13"; tag = "1.13.x" },
    @{ id = "v1_12"; tag = "1.12.x" },
    @{ id = "v1_11"; tag = "1.11.x" },
    @{ id = "v1_10"; tag = "1.10.x" },
    @{ id = "v1_9";  tag = "1.9.x"  },
    @{ id = "v1_8";  tag = "1.8.x"  }
)

# Eski Bukkit JAR'larını temizle
Remove-Item Manhunt-Bukkit_*.jar -ErrorAction SilentlyContinue

# Core modülünü önce yükle
Write-Host "Core modulu yukleniyor..." -ForegroundColor Cyan
mvn clean install -pl core -am -DskipTests -q

foreach ($profile in $profiles) {
    $id = $profile.id
    $tag = $profile.tag
    
    Write-Host "[$tag] Bukkit derleniyor..." -ForegroundColor Gray
    mvn clean package -P $id -pl bukkit -am -DskipTests -q
    
    if ($LASTEXITCODE -eq 0) {
        Move-Item "bukkit/target/manhunt-bukkit-1.1.jar" "Manhunt-Bukkit_$tag.jar" -Force
        Write-Host "[$tag] Bukkit hazir: Manhunt-Bukkit_$tag.jar" -ForegroundColor Green
    } else {
        Write-Host "[$tag] Bukkit derlenirken HATA olustu!" -ForegroundColor Red
    }
}

Write-Host "Bukkit build islemi tamamlandi." -ForegroundColor Yellow
