Write-Host "Manhunt SPONGE Build Scripti basliyor..." -ForegroundColor Cyan

$profiles = @(
    @{ id = "v1_21"; tag = "1.21.x" },
    @{ id = "v1_20"; tag = "1.20.x" },
    @{ id = "v1_19"; tag = "1.19.x" },
    @{ id = "v1_18"; tag = "1.18.x" },
    @{ id = "v1_17"; tag = "1.17.x" },
    @{ id = "v1_16"; tag = "1.16.x" },
    @{ id = "v1_15"; tag = "1.15.x" },
    @{ id = "v1_14"; tag = "1.14.x" },
    @{ id = "v1_13"; tag = "1.13.x" }
)

# Eski Sponge JAR'larını temizle
Remove-Item Manhunt-Sponge_*.jar -ErrorAction SilentlyContinue

# Core modülünü önce yükle (Sponge ve Bukkit buna bağımlıdır)
Write-Host "Core modulu yukleniyor..." -ForegroundColor Yellow
mvn clean install -pl core -am -DskipTests -q

foreach ($profile in $profiles) {
    $id = $profile.id
    $tag = $profile.tag
    
    Write-Host "[$tag] Sponge derleniyor..." -ForegroundColor Gray
    mvn clean package -P $id -pl sponge -am -DskipTests -q
    
    if ($LASTEXITCODE -eq 0) {
        # Sponge 8+ typically produces a jar with the artifactId-version format
        # Let's find the jar in the target folder
        $jar = Get-ChildItem "sponge/target/*.jar" | Where-Object { $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-javadoc.jar" } | Select-Object -First 1
        if ($jar) {
            Copy-Item $jar.FullName "Manhunt-Sponge_$tag.jar" -Force
            Write-Host "[$tag] Sponge hazir: Manhunt-Sponge_$tag.jar" -ForegroundColor Green
        } else {
             Write-Host "[$tag] Sponge JAR dosyasi bulunamadi!" -ForegroundColor Red
        }
    } else {
        Write-Host "[$tag] Sponge derlenirken HATA olustu! (API farkliliklari olabilir)" -ForegroundColor Red
    }
}

Write-Host "Sponge build islemi tamamlandi." -ForegroundColor Cyan
